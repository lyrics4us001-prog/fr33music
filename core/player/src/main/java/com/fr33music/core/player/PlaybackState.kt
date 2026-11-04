package com.fr33music.core.player

import com.fr33music.core.domain.model.Track

data class PlaybackState(
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isShuffle: Boolean = false,
    val error: String? = null
) {
    val currentTrack: Track?
        get() = queue.getOrNull(currentIndex)
}
