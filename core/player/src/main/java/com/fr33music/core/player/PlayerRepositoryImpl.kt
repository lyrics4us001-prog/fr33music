package com.fr33music.core.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
    private val streamResolver: StreamResolver
) : PlayerRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null

    init {
        ensureServiceRunning()
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startProgressTracker()
                } else {
                    stopProgressTracker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.update { it.copy(isLoading = true) }
                    }
                    Player.STATE_READY -> {
                        _playbackState.update { it.copy(isLoading = false, error = null) }
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

        if (existingIndex >= 0) {
            _playbackState.update { it.copy(currentIndex = existingIndex) }
            resolveAndPlay(track)
        } else {
            val updatedQueue = currentQueue + track
            val newIndex = updatedQueue.lastIndex
            _playbackState.update {
                it.copy(queue = updatedQueue, currentIndex = newIndex)
            }
            resolveAndPlay(track)
        }
    }

    override suspend fun setQueue(list: List<Track>, startIndex: Int) {
        if (list.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, list.lastIndex)
        _playbackState.update {
            it.copy(queue = list, currentIndex = validIndex)
        }
        resolveAndPlay(list[validIndex])
    }

    override fun togglePlayPause() {
        try {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                if (exoPlayer.playbackState == Player.STATE_IDLE && _playbackState.value.currentTrack != null) {
                    repositoryScope.launch {
                        _playbackState.value.currentTrack?.let { resolveAndPlay(it) }
                    }
                } else {
                    exoPlayer.play()
                }
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(error = e.message) }
        }
    }

    override suspend fun next() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return
        val nextIndex = (state.currentIndex + 1) % state.queue.size
        _playbackState.update { it.copy(currentIndex = nextIndex) }
        resolveAndPlay(state.queue[nextIndex])
    }

    override suspend fun previous() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (exoPlayer.currentPosition > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (state.currentIndex > 0) state.currentIndex - 1 else state.queue.lastIndex
        _playbackState.update { it.copy(currentIndex = prevIndex) }
        resolveAndPlay(state.queue[prevIndex])
    }

    override fun seekTo(positionMs: Long) {
        try {
            exoPlayer.seekTo(positionMs)
            _playbackState.update { it.copy(positionMs = positionMs) }
        } catch (e: Exception) {
            _playbackState.update { it.copy(error = e.message) }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun resolveAndPlay(track: Track) {
        _playbackState.update { it.copy(isLoading = true, error = null) }

        try {
            val result = streamResolver.resolveAudio(track)
            result.fold(
                onSuccess = { stream ->
                    try {
                        val mediaMetadata = MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artistName)
                            .setArtworkUri(track.thumbnailUrl?.let { Uri.parse(it) })
                            .build()

                        val mediaItem = MediaItem.Builder()
                            .setUri(stream.url)
                            .setMediaId(track.trackId)
                            .setMediaMetadata(mediaMetadata)
                            .build()

                        val userAgent = stream.headers["User-Agent"] ?: NewPipeDownloader.USER_AGENT
                        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                            .setUserAgent(userAgent)
                            .setDefaultRequestProperties(stream.headers)
                            .setAllowCrossProtocolRedirects(true)

                        val mediaSource = ProgressiveMediaSource.Factory(httpDataSourceFactory)
                            .createMediaSource(mediaItem)

                        exoPlayer.setMediaSource(mediaSource)
                        exoPlayer.prepare()
                        exoPlayer.play()

                        ensureServiceRunning()
                    } catch (e: Exception) {
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
        try {
            val serviceIntent = Intent(context, Fr33MusicPlayerService::class.java)
            context.startService(serviceIntent)
        } catch (_: Exception) {
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
            while (isActive) {
                val currentPos = exoPlayer.currentPosition
                _playbackState.update { it.copy(positionMs = currentPos) }
                delay(500L)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }
}
