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
     * Prioritizes OPUS streams with optimal bitrate (around 96kbps - 128kbps for fast streaming start, up to 160kbps).
     * If OPUS is not available, falls back to M4A with similar logic.
     * Returns null if no suitable OPUS or M4A stream is found.
     */
    fun selectBestStream(candidates: List<AudioCandidate>): AudioCandidate? {
        if (candidates.isEmpty()) return null

        val opusStreams = candidates.filter { it.format == AudioFormat.OPUS }
        if (opusStreams.isNotEmpty()) {
            return chooseOptimalStream(opusStreams)
        }

        val m4aStreams = candidates.filter { it.format == AudioFormat.M4A }
        if (m4aStreams.isNotEmpty()) {
            return chooseOptimalStream(m4aStreams)
        }

        return null
    }

    private fun chooseOptimalStream(streams: List<AudioCandidate>): AudioCandidate {
        // 1. Si hay streams en 96kbps..128kbps, priorizarlos para iniciar más rápido (seleccionando 128 si existe, o 96)
        val fastRange = streams.filter { it.bitrateKbps in 96..128 }
        if (fastRange.isNotEmpty()) {
            return fastRange.maxByOrNull { it.bitrateKbps }!!
        }

        // 2. Si no hay en 96..128, buscar en rango 128..160 (priorizando el menor dentro del rango para agilidad)
        val standardRange = streams.filter { it.bitrateKbps in 128..160 }
        if (standardRange.isNotEmpty()) {
            return standardRange.minByOrNull { it.bitrateKbps }!!
        }

        // 3. Fallback: la más cercana a 128kbps
        return streams.minByOrNull { abs(it.bitrateKbps - 128) }!!
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
