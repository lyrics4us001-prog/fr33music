package com.fr33music.core.domain.model

data class AlbumCard(
    val title: String,
    val artistName: String,
    val thumbnailUrl: String?,
    val songCount: Long = 0,
    val url: String? = null,
    val channelId: String? = null,
    val channelUrl: String? = null,
    val durationMs: Long = 0L
)
