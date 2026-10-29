package com.fr33music.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val trackId: String,
    val title: String,
    val artistName: String,
    val durationMs: Long,
    val thumbnailUrl: String? = null
)
