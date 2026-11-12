package com.fr33music.core.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.fr33music.core.data.AudioCacheManager
import com.fr33music.core.database.datastore.QueueDataStore
import com.fr33music.core.domain.model.Track
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
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val streamResolver: StreamResolver,
    private val queueDataStore: QueueDataStore,
    private val simpleCache: SimpleCache,
    private val audioCacheManager: AudioCacheManager
) : PlayerRepository {

    override val player: ExoPlayer get() = exoPlayer

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var _mediaSession: MediaSession? = null
    override val mediaSession: MediaSession? get() = _mediaSession

    override fun attachMediaSession(session: MediaSession) {
        _mediaSession = session
        Log.i("PlayerRepo", "MediaSession vinculada con PlayerRepository")
    }

    override fun detachMediaSession() {
        _mediaSession = null
        Log.i("PlayerRepo", "MediaSession desvinculada de PlayerRepository")
    }

    private var mediaController: MediaController? = null
    private var isConnectingController = false
    private val pendingControllerActions = mutableListOf<(MediaController) -> Unit>()

    private var progressJob: Job? = null
    private val playedTrackIds = mutableSetOf<String>()
    private val shuffleHistory = ArrayDeque<Track>()

    init {
        // 1. Configurar repeatMode por defecto a REPEAT_MODE_ALL para que hasNextMediaItem() siempre sea true
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        // 2. Iniciar servicio y conectar MediaController
        ensureServiceRunning()
        connectMediaController()

        // 3. Escuchar cambios de ExoPlayer directamente para sincronización inmediata de UI
        setupPlayerListener(exoPlayer)

        // 4. Restaurar estado de cola guardado en DataStore (sobrevive al reiniciar la app)
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
                exoPlayer.shuffleModeEnabled = savedState.isShuffle
                withController {
                    it.repeatMode = Player.REPEAT_MODE_ALL
                    it.shuffleModeEnabled = savedState.isShuffle
                }
            }
        }
    }

    private fun withController(action: (MediaController) -> Unit) {
        val controller = mediaController
        if (controller != null && controller.isConnected) {
            action(controller)
        } else {
            pendingControllerActions.add(action)
            connectMediaController()
        }
    }

    private fun connectMediaController() {
        if (isConnectingController) return
        val currentCtrl = mediaController
        if (currentCtrl != null && currentCtrl.isConnected) return

        isConnectingController = true
        ensureServiceRunning()

        val sessionToken = SessionToken(context, ComponentName(context, Fr33MusicPlayerService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            isConnectingController = false
            try {
                val controller = controllerFuture.get()
                mediaController = controller
                controller.repeatMode = Player.REPEAT_MODE_ALL
                controller.shuffleModeEnabled = _playbackState.value.isShuffle
                setupPlayerListener(controller)
                Log.i("PlayerRepo", "MediaController conectado exitosamente con Fr33MusicPlayerService (repeatMode=ALL, shuffle=${_playbackState.value.isShuffle})")

                val actions = ArrayList(pendingControllerActions)
                pendingControllerActions.clear()
                actions.forEach { it(controller) }
            } catch (e: Exception) {
                Log.e("PlayerRepo", "Error al conectar MediaController: ${e.message}", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupPlayerListener(playerTarget: Player) {
        playerTarget.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                    val pos = try { playerTarget.currentPosition } catch (e: Exception) { 0L }
                    repositoryScope.launch(Dispatchers.IO) {
                        queueDataStore.savePlaybackPosition(pos)
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.update { it.copy(isLoading = true) }
                    }
                    Player.STATE_READY -> {
                        val dur = try {
                            val d = playerTarget.duration
                            if (d > 0L && d != C.TIME_UNSET) d else 0L
                        } catch (e: Exception) { 0L }

                        _playbackState.update { current ->
                            val currentDur = if (dur > 0L) dur else current.durationMs
                            val updatedQueue = if (currentDur > 0L && current.currentTrack != null && current.currentTrack!!.durationMs <= 0L) {
                                current.queue.mapIndexed { idx, t ->
                                    if (idx == current.currentIndex) t.copy(durationMs = currentDur) else t
                                }
                            } else current.queue

                            current.copy(
                                isLoading = false,
                                error = null,
                                durationMs = currentDur,
                                queue = updatedQueue
                            )
                        }
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.update { it.copy(isLoading = false) }
                    }
                    Player.STATE_IDLE -> {
                        _playbackState.update { it.copy(isLoading = false) }
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId
                val currentQueue = _playbackState.value.queue
                val foundIndex = if (!mediaId.isNullOrBlank()) {
                    currentQueue.indexOfFirst { it.trackId == mediaId }
                } else {
                    playerTarget.currentMediaItemIndex
                }
                val validIndex = if (foundIndex in currentQueue.indices) foundIndex else playerTarget.currentMediaItemIndex

                _playbackState.update { current ->
                    val track = current.queue.getOrNull(validIndex)
                    current.copy(
                        currentIndex = validIndex,
                        durationMs = track?.durationMs?.takeIf { it > 0L } ?: current.durationMs
                    )
                }

                if (validIndex in currentQueue.indices) {
                    playedTrackIds.add(currentQueue[validIndex].trackId)
                    repositoryScope.launch(Dispatchers.IO) {
                        queueDataStore.saveCurrentIndex(validIndex)
                    }
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                val newPos = newPosition.positionMs.coerceAtLeast(0L)
                _playbackState.update { it.copy(positionMs = newPos) }
                repositoryScope.launch(Dispatchers.IO) {
                    queueDataStore.savePlaybackPosition(newPos)
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.update { it.copy(isShuffle = shuffleModeEnabled) }
                repositoryScope.launch(Dispatchers.IO) {
                    queueDataStore.saveShuffle(shuffleModeEnabled)
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                if (repeatMode != Player.REPEAT_MODE_ALL) {
                    withController { it.repeatMode = Player.REPEAT_MODE_ALL }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerRepo", "Error de reproducción: ${error.message} (código: ${error.errorCode})", error)
                val errorMessage = if (error.message?.contains("age-restricted", ignoreCase = true) == true ||
                    error.message?.contains("sign in to confirm your age", ignoreCase = true) == true
                ) {
                    "Este video tiene restricción de edad y no se puede reproducir de forma anónima."
                } else {
                    error.message ?: "Error al reproducir pista"
                }
                _playbackState.update {
                    it.copy(
                        isLoading = false,
                        isPlaying = false,
                        error = errorMessage
                    )
                }
            }
        })
    }

    private fun trackToMediaItem(track: Track): MediaItem {
        val downloadsDir = File(context.filesDir, "downloads")
        val localCoverFile = File(downloadsDir, "${track.trackId}.jpg")
        val trackThumb = track.thumbnailUrl
        val safeThumbnailUri = when {
            localCoverFile.exists() && localCoverFile.length() > 0 -> Uri.fromFile(localCoverFile)
            !trackThumb.isNullOrBlank() && !trackThumb.startsWith("file:") -> Uri.parse(trackThumb)
            else -> Uri.parse("https://i.ytimg.com/vi/${track.trackId}/hqdefault.jpg")
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artistName)
            .apply {
                if (track.durationMs > 0L) {
                    setDurationMs(track.durationMs)
                }
                setArtworkUri(safeThumbnailUri)
            }
            .build()

        return MediaItem.Builder()
            .setMediaId(track.trackId)
            .setUri(Uri.parse("fr33music://track/${track.trackId}"))
            .setMediaMetadata(metadata)
            .build()
    }

    override suspend fun play(track: Track) {
        val currentQueue = _playbackState.value.queue
        val existingIndex = currentQueue.indexOfFirst { it.trackId == track.trackId }

        val newQueue: List<Track>
        val targetIndex: Int
        if (existingIndex >= 0) {
            newQueue = currentQueue
            targetIndex = existingIndex
        } else {
            newQueue = currentQueue + track
            targetIndex = newQueue.lastIndex
        }

        _playbackState.update {
            it.copy(
                queue = newQueue,
                currentIndex = targetIndex,
                isPlaying = true,
                isLoading = true,
                error = null
            )
        }
        playedTrackIds.add(track.trackId)
        shuffleHistory.clear()

        repositoryScope.launch(Dispatchers.IO) {
            queueDataStore.saveQueue(newQueue, targetIndex, _playbackState.value.isShuffle)
        }

        withContext(Dispatchers.Main) {
            withController { controller ->
                controller.repeatMode = Player.REPEAT_MODE_ALL
                controller.shuffleModeEnabled = _playbackState.value.isShuffle
                if (existingIndex >= 0 && controller.mediaItemCount == newQueue.size) {
                    controller.seekTo(targetIndex, 0L)
                    controller.play()
                } else {
                    val mediaItems = newQueue.map { trackToMediaItem(it) }
                    controller.setMediaItems(mediaItems, targetIndex, 0L)
                    controller.prepare()
                    controller.play()
                }
            }
        }
    }

    override suspend fun setQueue(list: List<Track>, startIndex: Int) {
        if (list.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, list.lastIndex)
        _playbackState.update {
            it.copy(
                queue = list,
                currentIndex = validIndex,
                isPlaying = true,
                isLoading = true,
                error = null
            )
        }

        repositoryScope.launch(Dispatchers.IO) {
            queueDataStore.saveQueue(list, validIndex, _playbackState.value.isShuffle)
        }

        val mediaItems = list.map { trackToMediaItem(it) }

        withContext(Dispatchers.Main) {
            withController { controller ->
                controller.repeatMode = Player.REPEAT_MODE_ALL
                controller.shuffleModeEnabled = _playbackState.value.isShuffle
                controller.setMediaItems(mediaItems, validIndex, 0L)
                controller.prepare()
                controller.play()
            }
        }
    }

    override fun togglePlayPause() {
        withController { controller ->
            if (controller.isPlaying || controller.playWhenReady) {
                controller.pause()
            } else {
                if (controller.playbackState == Player.STATE_IDLE || controller.mediaItemCount == 0) {
                    val queue = _playbackState.value.queue
                    if (queue.isNotEmpty()) {
                        val index = _playbackState.value.currentIndex.coerceIn(0, queue.lastIndex)
                        val mediaItems = queue.map { trackToMediaItem(it) }
                        controller.repeatMode = Player.REPEAT_MODE_ALL
                        controller.shuffleModeEnabled = _playbackState.value.isShuffle
                        controller.setMediaItems(mediaItems, index, _playbackState.value.positionMs)
                        controller.prepare()
                        controller.play()
                    }
                } else {
                    controller.play()
                }
            }
        }
    }

    override fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffle
        _playbackState.update { it.copy(isShuffle = newShuffle) }
        withController { controller ->
            controller.shuffleModeEnabled = newShuffle
        }
        repositoryScope.launch {
            queueDataStore.saveShuffle(newShuffle)
        }
    }

    override suspend fun next() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        withContext(Dispatchers.Main) {
            withController { controller ->
                if (controller.mediaItemCount != state.queue.size) {
                    val mediaItems = state.queue.map { trackToMediaItem(it) }
                    val nextIdx = (state.currentIndex + 1) % state.queue.size
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    controller.shuffleModeEnabled = state.isShuffle
                    controller.setMediaItems(mediaItems, nextIdx, 0L)
                    controller.prepare()
                    controller.play()
                } else {
                    controller.seekToNextMediaItem()
                    controller.play()
                }
            }
        }
    }

    override suspend fun previous() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        withContext(Dispatchers.Main) {
            withController { controller ->
                val currentPos = try { controller.currentPosition } catch (e: Exception) { 0L }
                if (currentPos > 3000L) {
                    controller.seekTo(0L)
                    controller.play()
                    return@withController
                }

                if (controller.mediaItemCount != state.queue.size) {
                    val mediaItems = state.queue.map { trackToMediaItem(it) }
                    val prevIdx = if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.lastIndex
                    controller.repeatMode = Player.REPEAT_MODE_ALL
                    controller.shuffleModeEnabled = state.isShuffle
                    controller.setMediaItems(mediaItems, prevIdx, 0L)
                    controller.prepare()
                    controller.play()
                } else {
                    controller.seekToPreviousMediaItem()
                    controller.play()
                }
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        withController { controller ->
            try {
                val totalDuration = _playbackState.value.durationMs.takeIf { it > 0L }
                    ?: _playbackState.value.currentTrack?.durationMs?.takeIf { it > 0L }
                    ?: try {
                        val d = controller.duration
                        if (d > 0L && d != C.TIME_UNSET) d else null
                    } catch (_: Exception) { null }

                val validPos = when {
                    positionMs < 0L -> 0L
                    totalDuration != null && totalDuration > 1000L && positionMs >= totalDuration -> (totalDuration - 1000L).coerceAtLeast(0L)
                    totalDuration != null && positionMs > totalDuration -> (totalDuration - 1000L).coerceAtLeast(0L)
                    else -> positionMs.coerceAtLeast(0L)
                }

                controller.seekTo(validPos)
                _playbackState.update { it.copy(positionMs = validPos) }
                repositoryScope.launch(Dispatchers.IO) {
                    queueDataStore.savePlaybackPosition(validPos)
                }
            } catch (e: Exception) {
                Log.w("PlayerRepo", "Error en seekTo($positionMs): ${e.message}", e)
            }
        }
    }

    private fun ensureServiceRunning() {
        if (Fr33MusicPlayerService.isRunning) return
        try {
            val serviceIntent = Intent(context, Fr33MusicPlayerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e("PlayerRepo", "No se pudo iniciar el servicio de reproducción: ${e.message}", e)
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = repositoryScope.launch(Dispatchers.Main) {
            var counter = 0
            while (isActive) {
                val playerTarget = mediaController ?: exoPlayer
                val currentPos = try { playerTarget.currentPosition.coerceAtLeast(0L) } catch (e: Exception) { 0L }
                val currentDur = try {
                    val d = playerTarget.duration
                    if (d > 0L && d != C.TIME_UNSET) d else 0L
                } catch (e: Exception) { 0L }

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
                if (counter % 4 == 0) {
                    launch(Dispatchers.IO) {
                        queueDataStore.savePlaybackPosition(currentPos)
                    }
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
