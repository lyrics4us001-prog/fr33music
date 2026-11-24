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
            // Mejorar miniaturas de baja calidad a hqdefault (480x360), garantizada en todo YouTube.
            // NO forzar maxresdefault.jpg porque devuelve 404 Not Found en pistas de audio y videos < 720p.
            if (result.contains("/default.jpg")) result = result.replace("/default.jpg", "/hqdefault.jpg")
            if (result.contains("/mqdefault.jpg")) result = result.replace("/mqdefault.jpg", "/hqdefault.jpg")
            // Para URLs de avatares de Google/YouTube (=w o =s), aumentar resolución preservando tokens/parámetros
            if (result.contains("=w") || result.contains("=s")) {
                result = result.replace(Regex("=w\\d+-h\\d+"), "=w512-h512")
                    .replace(Regex("=s\\d+"), "=s512")
            }
            return result
        }
    }
}
