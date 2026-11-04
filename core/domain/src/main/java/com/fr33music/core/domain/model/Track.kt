package com.fr33music.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val trackId: String,
    val title: String,
    val artistName: String,
    val durationMs: Long,
    val thumbnailUrl: String? = null,
    val albumName: String? = null,
    val artistThumbnailUrl: String? = null
) {
    companion object {
        fun upgradeThumbnail(url: String?): String? {
            if (url.isNullOrBlank()) return url
            var result = url
            if (result.contains("/default.jpg")) result = result.replace("/default.jpg", "/maxresdefault.jpg")
            if (result.contains("/mqdefault.jpg")) result = result.replace("/mqdefault.jpg", "/maxresdefault.jpg")
            if (result.contains("/hqdefault.jpg")) result = result.replace("/hqdefault.jpg", "/maxresdefault.jpg")
            if (result.contains("/sddefault.jpg")) result = result.replace("/sddefault.jpg", "/maxresdefault.jpg")
            if (result.contains("hq720.jpg")) result = result.replace("hq720.jpg", "maxresdefault.jpg")
            if (result.contains("=w") || result.contains("=s")) {
                result = result.replace(Regex("=w\\d+-h\\d+.*"), "=w1024-h1024-l90-rj")
                    .replace(Regex("=w\\d+.*"), "=w1024-h1024-l90-rj")
                    .replace(Regex("=s\\d+.*"), "=s1024")
            }
            return result
        }
    }
}
