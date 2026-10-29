package com.fr33music.core.domain.model

data class AudioStream(
    val url: String,
    val headers: Map<String, String>,
    val mime: String,
    val bitrateKbps: Int,
    val format: AudioFormat
)
