package com.fr33music.feature.player

import com.fr33music.core.domain.model.Track

data class PlayerUiState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isShuffle: Boolean = false,
    val isSaved: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadStatus: String? = null,
    val lyrics: String? = null,
    val isLoadingLyrics: Boolean = false,
    val showLyricsSheet: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)
