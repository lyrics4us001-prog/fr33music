package com.fr33music.core.player

import androidx.media3.session.MediaSession
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val player: androidx.media3.exoplayer.ExoPlayer
    val playbackState: StateFlow<PlaybackState>
    val mediaSession: MediaSession?
    fun attachMediaSession(session: MediaSession)
    fun detachMediaSession()
    suspend fun play(track: Track)
    suspend fun setQueue(list: List<Track>, startIndex: Int = 0)
    fun togglePlayPause()
    fun toggleShuffle()
    suspend fun next()
    suspend fun previous()
    fun seekTo(positionMs: Long)
}

fun MediaSession.setQueue(tracks: List<Track>) {
    val extras = (sessionExtras ?: android.os.Bundle()).deepCopy().apply {
        putInt("queue_size", tracks.size)
        putStringArrayList("queue_ids", ArrayList(tracks.map { it.trackId }))
    }
    setSessionExtras(extras)
}
