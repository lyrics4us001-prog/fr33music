package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioStream
import com.fr33music.core.domain.model.Track

data class ResolvedMetadata(
    val title: String,
    val artistName: String,
    val durationMs: Long,
    val thumbnailUrl: String? = null
)

interface StreamResolver {
    suspend fun resolveAudio(track: Track): Result<AudioStream>
    suspend fun resolveMetadata(videoId: String): Result<ResolvedMetadata>
}
