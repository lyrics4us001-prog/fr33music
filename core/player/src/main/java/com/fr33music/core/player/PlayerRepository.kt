package com.fr33music.core.player

import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    val playbackState: StateFlow<PlaybackState>
    suspend fun play(track: Track)
    suspend fun setQueue(list: List<Track>, startIndex: Int = 0)
    fun togglePlayPause()
    fun toggleShuffle()
    suspend fun next()
    suspend fun previous()
    fun seekTo(positionMs: Long)
}
