package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioFormat

data class AudioCandidate(
    val url: String,
    val mime: String,
    val bitrateKbps: Int,
    val format: AudioFormat
)

object AudioStreamSelector {

    /**
     * Prioritizes OPUS streams. If OPUS is not available, falls back to M4A.
     * Within the same format, picks the one with the highest bitrate.
     * Returns null if no suitable OPUS or M4A stream is found.
     */
    fun selectBestStream(candidates: List<AudioCandidate>): AudioCandidate? {
        if (candidates.isEmpty()) return null

        val opusStreams = candidates
            .filter { it.format == AudioFormat.OPUS }
            .sortedByDescending { it.bitrateKbps }

        if (opusStreams.isNotEmpty()) {
            return opusStreams.first()
        }

        val m4aStreams = candidates
            .filter { it.format == AudioFormat.M4A }
            .sortedByDescending { it.bitrateKbps }

        if (m4aStreams.isNotEmpty()) {
            return m4aStreams.first()
        }

        return null
    }

    fun parseFormat(formatName: String?, mimeType: String?): AudioFormat {
        val fName = formatName?.uppercase() ?: ""
        val mType = mimeType?.lowercase() ?: ""

        return when {
            fName.contains("OPUS") || mType.contains("opus") || mType.contains("webm") -> AudioFormat.OPUS
            fName.contains("M4A") || fName.contains("MP4") || mType.contains("mp4") || mType.contains("m4a") || mType.contains("aac") -> AudioFormat.M4A
            else -> AudioFormat.UNKNOWN
        }
    }
}
