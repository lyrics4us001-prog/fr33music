package com.fr33music.core.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.fr33music.core.database.datastore.QueueDataStore
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.NewPipeDownloader
import com.fr33music.core.extractor.StreamResolver
import com.fr33music.core.player.service.Fr33MusicPlayerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val streamResolver: StreamResolver,
    private val queueDataStore: QueueDataStore,
    private val simpleCache: SimpleCache
) : PlayerRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private val playedTrackIds = mutableSetOf<String>()
    private val shuffleHistory = ArrayDeque<Track>()
    private var lastFailedTrackId: String? = null
    private var playerRetryCount = 0

    init {
        ensureServiceRunning()

        // Restaurar estado de cola guardado en DataStore (sobrevive al cerrar la app)
        repositoryScope.launch {
            val savedState = queueDataStore.getQueueStateSync()
            if (savedState.queue.isNotEmpty()) {
                val validIndex = savedState.currentIndex.coerceIn(0, savedState.queue.lastIndex)
                _playbackState.update {
                    it.copy(
                        queue = savedState.queue,
                        currentIndex = validIndex,
                        positionMs = savedState.positionMs,
                        isShuffle = savedState.isShuffle,
                        isPlaying = false
                    )
                }
                if (savedState.isShuffle) {
                    savedState.queue.getOrNull(validIndex)?.let { playedTrackIds.add(it.trackId) }
                }
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    repositoryScope.launch {
                        queueDataStore.savePlaybackPosition(exoPlayer.currentPosition)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.update { it.copy(isLoading = true) }
                    }
                    Player.STATE_READY -> {
                        playerRetryCount = 0
                        lastFailedTrackId = null
                        val currentDur = exoPlayer.duration.coerceAtLeast(0L)
                        _playbackState.update { current ->
                            val updatedQueue = if (currentDur > 0L && current.currentTrack != null && current.currentTrack!!.durationMs <= 0L) {
                                current.queue.mapIndexed { idx, t ->
                                    if (idx == current.currentIndex) t.copy(durationMs = currentDur) else t
                                }
                            } else current.queue

                            current.copy(
                                isLoading = false,
                                error = null,
                                durationMs = if (currentDur > 0L) currentDur else current.durationMs,
                                queue = updatedQueue
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(isLoading = false) }
                        repositoryScope.launch { next() }
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.update { it.copy(isLoading = false) }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerRepo", "ExoPlayer onPlayerError: ${error.message} (código: ${error.errorCode})", error)
                val currentTrack = _playbackState.value.currentTrack
                val is403OrHttp = error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ||
                        error.message?.contains("403") == true ||
                        error.cause?.message?.contains("403") == true ||
                        error.cause?.javaClass?.simpleName?.contains("HttpDataSource") == true

                if (is403OrHttp && currentTrack != null && (lastFailedTrackId != currentTrack.trackId || playerRetryCount < 2)) {
                    if (lastFailedTrackId != currentTrack.trackId) {
                        lastFailedTrackId = currentTrack.trackId
                        playerRetryCount = 1
                    } else {
                        playerRetryCount++
                    }
                    val pos = exoPlayer.currentPosition.coerceAtLeast(0L)
                    Log.w("PlayerRepo", "Detectado error 403 / HTTP en pista ${currentTrack.trackId}. Reintentando con stream fresco (intento $playerRetryCount/2)...")
                    repositoryScope.launch {
                        delay(600)
                        resolveAndPlay(currentTrack, pos)
                    }
                    return
                }

                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = false,
                        error = error.message ?: "Error de reproducción"
                    )
                }
            }
        })
    }

    override suspend fun play(track: Track) {
        val currentQueue = _playbackState.value.queue
        val existingIndex = currentQueue.indexOfFirst { it.trackId == track.trackId }

        val newQueue: List<Track>
        val newIndex: Int
        if (existingIndex >= 0) {
            newQueue = currentQueue
            newIndex = existingIndex
        } else {
            newQueue = currentQueue + track
            newIndex = newQueue.lastIndex
        }

        _playbackState.update {
            it.copy(queue = newQueue, currentIndex = newIndex)
        }
        playedTrackIds.add(track.trackId)
        shuffleHistory.clear()
        queueDataStore.saveQueue(newQueue, newIndex, _playbackState.value.isShuffle)
        resolveAndPlay(track)
    }

    override suspend fun setQueue(list: List<Track>, startIndex: Int) {
        if (list.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, list.lastIndex)
        _playbackState.update {
            it.copy(queue = list, currentIndex = validIndex)
        }
        playedTrackIds.clear()
        playedTrackIds.add(list[validIndex].trackId)
        shuffleHistory.clear()
        queueDataStore.saveQueue(list, validIndex, _playbackState.value.isShuffle)
        resolveAndPlay(list[validIndex])
    }

    override fun togglePlayPause() {
        try {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.playbackState == Player.STATE_IDLE && _playbackState.value.currentTrack != null) {
                    repositoryScope.launch {
                        val track = _playbackState.value.currentTrack ?: return@launch
                        resolveAndPlay(track, initialPositionMs = _playbackState.value.positionMs)
                    }
                } else {
                    exoPlayer.play()
                }
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(error = e.message) }
        }
    }

    override fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffle
        _playbackState.update { it.copy(isShuffle = newShuffle) }
        playedTrackIds.clear()
        shuffleHistory.clear()
        if (newShuffle) {
            _playbackState.value.currentTrack?.let { playedTrackIds.add(it.trackId) }
        }
        repositoryScope.launch {
            queueDataStore.saveShuffle(newShuffle)
        }
    }

    override suspend fun next() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        val nextIndex: Int
        if (state.isShuffle) {
            // Guardar canción actual en shuffleHistory antes de avanzar
            state.currentTrack?.let { current ->
                shuffleHistory.addLast(current)
            }

            // Regla de Shuffle: aleatorio de los que no están en el Set. Si se agota, limpiar y empezar
            val unplayed = state.queue.filter { it.trackId !in playedTrackIds }
            val chosenTrack = if (unplayed.isNotEmpty()) {
                unplayed.random()
            } else {
                playedTrackIds.clear()
                state.currentTrack?.let { playedTrackIds.add(it.trackId) }
                val remaining = state.queue.filter { it.trackId !in playedTrackIds }
                remaining.randomOrNull() ?: state.currentTrack ?: state.queue.firstOrNull()
            }
            if (chosenTrack == null) return
            playedTrackIds.add(chosenTrack.trackId)
            val foundIdx = state.queue.indexOfFirst { it.trackId == chosenTrack.trackId }
            nextIndex = if (foundIdx in state.queue.indices) foundIdx else 0
        } else {
            nextIndex = (state.currentIndex + 1) % state.queue.size
        }

        if (nextIndex in state.queue.indices) {
            _playbackState.update { it.copy(currentIndex = nextIndex) }
            queueDataStore.saveCurrentIndex(nextIndex)
            resolveAndPlay(state.queue[nextIndex])
        }
    }

    override suspend fun previous() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }

        // Si Shuffle está activo y hay historial, volver a la canción anterior del historial
        if (state.isShuffle && shuffleHistory.isNotEmpty()) {
            val prevTrack = shuffleHistory.removeLast()
            val foundIdx = state.queue.indexOfFirst { it.trackId == prevTrack.trackId }
            val prevIndex = if (foundIdx in state.queue.indices) foundIdx else state.currentIndex
            _playbackState.update { it.copy(currentIndex = prevIndex) }
            queueDataStore.saveCurrentIndex(prevIndex)
            resolveAndPlay(prevTrack)
            return
        }

        val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.lastIndex
        _playbackState.update { it.copy(currentIndex = prevIndex) }
        queueDataStore.saveCurrentIndex(prevIndex)
        resolveAndPlay(state.queue[prevIndex])
    }

    override fun seekTo(positionMs: Long) {
        try {
            exoPlayer.seekTo(positionMs)
            _playbackState.update { it.copy(positionMs = positionMs) }
            repositoryScope.launch {
                queueDataStore.savePlaybackPosition(positionMs)
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(error = e.message) }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun resolveAndPlay(track: Track, initialPositionMs: Long = 0L) {
        _playbackState.update { it.copy(isLoading = true, error = null) }

        try {
            val result = streamResolver.resolveAudio(track)
            result.fold(
                onSuccess = { stream ->
                    try {
                        val parsedUri = Uri.parse(stream.url)
                        val isLocalFile = stream.url.startsWith("file://") ||
                                stream.url.startsWith("file:/") ||
                                stream.url.startsWith("content://") ||
                                parsedUri.scheme == "file" ||
                                parsedUri.scheme == "content"

                        val isLocalArtwork = track.thumbnailUrl?.startsWith("file:") == true || track.thumbnailUrl?.startsWith("content:") == true

                        // 1. Cargar carátula en ByteArray ANTES de construir el MediaItem
                        // usando la caché de disco de Coil (funciona offline) para evitar cuadros blancos.
                        val artworkBytes = loadArtworkBytes(track.thumbnailUrl)

                        val mediaMetadataBuilder = MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artistName)

                        if (artworkBytes != null) {
                            mediaMetadataBuilder.setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                        }

                        if (isLocalArtwork && !track.thumbnailUrl.isNullOrBlank()) {
                            mediaMetadataBuilder.setArtworkUri(Uri.parse(track.thumbnailUrl))
                        }
                        val metadata = mediaMetadataBuilder.build()

                        val mediaItem = MediaItem.Builder()
                            .setUri(parsedUri)
                            .setMediaId(track.trackId)
                            .setMediaMetadata(metadata)
                            .build()

                        // 2. Configurar la fuente de datos:
                        // Si la URI empieza con file:// o content://, usar DefaultDataSource.Factory(context) SIN headers de red ni caché.
                        // Si la URI empieza con http:// o https://, usar DefaultHttpDataSource.Factory envuelto en CacheDataSource.Factory.
                        val dataSourceFactory = if (isLocalFile) {
                            Log.i("PlayerRepo", "Reproduciendo archivo local sin headers de red ni caché: ${stream.url}")
                            DefaultDataSource.Factory(context)
                        } else {
                            Log.i("PlayerRepo", "Reproduciendo stream remoto desde YouTube con caché y timeouts HTTP: ${stream.url}")
                            val userAgent = stream.headers["User-Agent"] ?: NewPipeDownloader.USER_AGENT
                            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                                .setUserAgent(userAgent)
                                .setDefaultRequestProperties(stream.headers)
                                .setAllowCrossProtocolRedirects(true)
                                .setConnectTimeoutMs(15000)
                                .setReadTimeoutMs(15000)

                            val cacheDataSourceFactory = CacheDataSource.Factory()
                                .setCache(simpleCache)
                                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

                            DefaultDataSource.Factory(context, cacheDataSourceFactory)
                        }

                        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(mediaItem)

                        exoPlayer.setMediaSource(mediaSource)
                        exoPlayer.playlistMetadata = metadata
                        exoPlayer.prepare()
                        if (initialPositionMs > 0L) {
                            exoPlayer.seekTo(initialPositionMs)
                        }
                        exoPlayer.play()

                        ensureServiceRunning()

                        // 3. Si artworkBytes no se cargó de inmediato, intentar cargar en segundo plano y actualizar MediaItem
                        if (artworkBytes == null && !track.thumbnailUrl.isNullOrBlank()) {
                            repositoryScope.launch(Dispatchers.IO) {
                                val bytes = loadArtworkBytes(track.thumbnailUrl)
                                if (bytes != null && _playbackState.value.currentTrack?.trackId == track.trackId) {
                                    val updatedMetadata = MediaMetadata.Builder()
                                        .setTitle(track.title)
                                        .setArtist(track.artistName)
                                        .setArtworkData(bytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                        .build()

                                    withContext(Dispatchers.Main) {
                                        exoPlayer.playlistMetadata = updatedMetadata
                                        val currentItem = exoPlayer.currentMediaItem
                                        if (currentItem != null) {
                                            val updatedItem = currentItem.buildUpon()
                                                .setMediaMetadata(updatedMetadata)
                                                .build()
                                            exoPlayer.replaceMediaItem(exoPlayer.currentMediaItemIndex, updatedItem)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PlayerRepo", "Error al inicializar el reproductor: ${e.message}", e)
                        _playbackState.update {
                            it.copy(
                                isLoading = false,
                                isPlaying = false,
                                error = e.message ?: "Error al inicializar el reproductor"
                            )
                        }
                    }
                },
                onFailure = { throwable ->
                    Log.e("PlayerRepo", "Fallo al resolver audio para ${track.trackId}: ${throwable.message}", throwable)
                    _playbackState.update {
                        it.copy(
                            isLoading = false,
                            isPlaying = false,
                            error = throwable.message ?: "No se pudo reproducir este tema"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("PlayerRepo", "Excepción al resolver y reproducir: ${e.message}", e)
            _playbackState.update {
                it.copy(
                    isLoading = false,
                    isPlaying = false,
                    error = e.message ?: "Error al obtener el stream de audio"
                )
            }
        }
    }

    private fun ensureServiceRunning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val serviceIntent = Intent(context, Fr33MusicPlayerService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (_: Exception) {
                // Service already running or background restrictions
            }
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = repositoryScope.launch {
            var counter = 0
            while (isActive) {
                val currentPos = exoPlayer.currentPosition
                val currentDur = exoPlayer.duration.coerceAtLeast(0L)
                _playbackState.update { current ->
                    val updatedQueue = if (currentDur > 0L && current.currentTrack != null && current.currentTrack!!.durationMs <= 0L) {
                        current.queue.mapIndexed { idx, t ->
                            if (idx == current.currentIndex) t.copy(durationMs = currentDur) else t
                        }
                    } else current.queue

                    current.copy(
                        positionMs = currentPos,
                        durationMs = if (currentDur > 0L) currentDur else current.durationMs,
                        queue = updatedQueue
                    )
                }
                counter++
                if (counter % 4 == 0) { // Persistir posición cada 2 segundos
                    queueDataStore.savePlaybackPosition(currentPos)
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private suspend fun loadArtworkBytes(url: String?): ByteArray? {
        if (url.isNullOrBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val loader = coil.Coil.imageLoader(context)
                val request = coil.request.ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                val drawable = result.drawable ?: return@withContext null
                val bitmap = if (drawable is android.graphics.drawable.BitmapDrawable) {
                    drawable.bitmap
                } else {
                    val bmp = android.graphics.Bitmap.createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(1),
                        drawable.intrinsicHeight.coerceAtLeast(1),
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp
                }
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
                stream.toByteArray()
            } catch (e: Exception) {
                Log.e("PlayerRepo", "Error cargando carátula para notificación: ${e.message}")
                null
            }
        }
    }
}
