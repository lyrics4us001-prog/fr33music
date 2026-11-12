package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AudioStreamSelectorTest {

    @Test
    fun `fixture 1 - OPUS y M4A disponibles - prioriza OPUS en rango 96-128kbps para streaming veloz`() {
        val candidates = listOf(
            AudioCandidate(
                url = "https://audio.stream/m4a_128",
                mime = "audio/mp4",
                bitrateKbps = 128,
                format = AudioFormat.M4A
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_160",
                mime = "audio/webm",
                bitrateKbps = 160,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_96",
                mime = "audio/webm",
                bitrateKbps = 96,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/m4a_256",
                mime = "audio/mp4",
                bitrateKbps = 256,
                format = AudioFormat.M4A
            )
        )

        val selected = AudioStreamSelector.selectBestStream(candidates)

        assertNotNull(selected)
        assertEquals(AudioFormat.OPUS, selected?.format)
        // Entre 96 y 160, debe priorizar 96 para iniciar streaming rápido
        assertEquals(96, selected?.bitrateKbps)
        assertEquals("https://audio.stream/opus_96", selected?.url)
    }

    @Test
    fun `fixture 2 - solo M4A disponible - selecciona M4A en rango optimo para inicio rapido`() {
        val candidates = listOf(
            AudioCandidate(
                url = "https://audio.stream/m4a_128",
                mime = "audio/mp4",
                bitrateKbps = 128,
                format = AudioFormat.M4A
            ),
            AudioCandidate(
                url = "https://audio.stream/m4a_256",
                mime = "audio/mp4",
                bitrateKbps = 256,
                format = AudioFormat.M4A
            ),
            AudioCandidate(
                url = "https://audio.stream/unknown_format",
                mime = "audio/unknown",
                bitrateKbps = 320,
                format = AudioFormat.UNKNOWN
            )
        )

        val selected = AudioStreamSelector.selectBestStream(candidates)

        assertNotNull(selected)
        assertEquals(AudioFormat.M4A, selected?.format)
        // 128kbps debe ser priorizado sobre 256kbps para arranque rápido
        assertEquals(128, selected?.bitrateKbps)
        assertEquals("https://audio.stream/m4a_128", selected?.url)
    }

    @Test
    fun `fixture 3 - ninguno disponible o lista vacia - devuelve null`() {
        val emptyCandidates = emptyList<AudioCandidate>()
        val selectedEmpty = AudioStreamSelector.selectBestStream(emptyCandidates)
        assertNull(selectedEmpty)

        val onlyUnknown = listOf(
            AudioCandidate(
                url = "https://audio.stream/unknown",
                mime = "audio/unknown",
                bitrateKbps = 128,
                format = AudioFormat.UNKNOWN
            )
        )
        val selectedUnknown = AudioStreamSelector.selectBestStream(onlyUnknown)
        assertNull(selectedUnknown)
    }

    @Test
    fun `OPUS disponible en rango optimo 128-160 kbps - prioriza 128 sobre 160 y 256 para arranque rapido`() {
        val candidates = listOf(
            AudioCandidate(
                url = "https://audio.stream/opus_256",
                mime = "audio/webm",
                bitrateKbps = 256,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_160",
                mime = "audio/webm",
                bitrateKbps = 160,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_128",
                mime = "audio/webm",
                bitrateKbps = 128,
                format = AudioFormat.OPUS
            )
        )

        val selected = AudioStreamSelector.selectBestStream(candidates)

        assertNotNull(selected)
        assertEquals(AudioFormat.OPUS, selected?.format)
        assertEquals(128, selected?.bitrateKbps)
        assertEquals("https://audio.stream/opus_128", selected?.url)
    }

    @Test
    fun `OPUS fuera del rango optimo - selecciona el mas cercano a 160kbps`() {
        val candidates = listOf(
            AudioCandidate(
                url = "https://audio.stream/opus_50",
                mime = "audio/webm",
                bitrateKbps = 50,
                format = AudioFormat.OPUS
            ),
            AudioCandidate(
                url = "https://audio.stream/opus_96",
                mime = "audio/webm",
                bitrateKbps = 96,
                format = AudioFormat.OPUS
            )
        )

        val selected = AudioStreamSelector.selectBestStream(candidates)

        assertNotNull(selected)
        assertEquals(AudioFormat.OPUS, selected?.format)
        assertEquals(96, selected?.bitrateKbps)
    }

    @Test
    fun `parseFormat reconoce OPUS y M4A correctamente`() {
        assertEquals(AudioFormat.OPUS, AudioStreamSelector.parseFormat("WEBMA_OPUS", "audio/webm"))
        assertEquals(AudioFormat.OPUS, AudioStreamSelector.parseFormat("opus", "audio/opus"))
        assertEquals(AudioFormat.M4A, AudioStreamSelector.parseFormat("M4A", "audio/mp4"))
        assertEquals(AudioFormat.M4A, AudioStreamSelector.parseFormat("AAC", "audio/aac"))
        assertEquals(AudioFormat.UNKNOWN, AudioStreamSelector.parseFormat("OGG", "audio/ogg"))
    }
}
