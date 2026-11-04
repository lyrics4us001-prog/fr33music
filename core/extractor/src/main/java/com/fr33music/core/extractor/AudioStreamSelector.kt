package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioFormat
import kotlin.math.abs

data class AudioCandidate(
    val url: String,
    val mime: String,
    val bitrateKbps: Int,
    val format: AudioFormat
)

object AudioStreamSelector {

    /**
     * Prioritizes OPUS streams with optimal bitrate (around 128kbps - 160kbps).
     * If OPUS is not available, falls back to M4A.
     * Returns null if no suitable OPUS or M4A stream is found.
     */
    fun selectBestStream(candidates: List<AudioCandidate>): AudioCandidate? {
        if (candidates.isEmpty()) return null

        val opusStreams = candidates.filter { it.format == AudioFormat.OPUS }
        if (opusStreams.isNotEmpty()) {
            // Priorizar stream en rango óptimo (128kbps - 160kbps Opus).
            // Si hay varios en ese rango, tomar el de mayor bitrate (ej. 160kbps).
            val inOptimalRange = opusStreams.filter { it.bitrateKbps in 128..160 }
            if (inOptimalRange.isNotEmpty()) {
                return inOptimalRange.maxByOrNull { it.bitrateKbps }
            }
            // Si ninguno está en el rango 128..160, tomar el más cercano a 160kbps
            return opusStreams.minByOrNull { abs(it.bitrateKbps - 160) }
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
