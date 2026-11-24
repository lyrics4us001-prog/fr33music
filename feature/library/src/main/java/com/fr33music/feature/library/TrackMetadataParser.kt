package com.fr33music.feature.library

object TrackMetadataParser {

    private val TOPIC_SUFFIX_REGEX = Regex("(?i)\\s*-\\s*topic$")
    private val DASH_REGEX = Regex("\\s+[-–—]\\s+")
    private val FEAT_REGEX = Regex("(?i)\\b(feat\\.?|ft\\.?|con)\\b")
    private val BRACKET_REGEX = Regex("([\\[(])([^\\])]*)([\\])])")
    private val JUNK_KEYWORD_REGEX = Regex(
        "(?i)\\b(oficial|official|video|audio|visualizer|lyric|lyrics|letra|clip|hd|hq|4k|remastered|remasterizado|music\\s*video|en\\s*vivo|live)\\b"
    )
    private val STANDALONE_JUNK = listOf(
        Regex("(?i)\\s*\\|\\s*(official\\s*music\\s*video|official\\s*video|video\\s*oficial|official\\s*audio|audio\\s*oficial|lyric\\s*video|visualizer).*$"),
        Regex("(?i)\\s*-\\s*(official\\s*music\\s*video|official\\s*video|video\\s*oficial|official\\s*audio|audio\\s*oficial|lyric\\s*video|visualizer).*$"),
        Regex("(?i)\\s*-\\s*topic$")
    )
    private val EMPTY_BRACKETS_REGEX = Regex("(\\[\\s*\\]|\\(\\s*\\))")
    private val WHITESPACE_REGEX = Regex("\\s+")

    fun parse(rawTitle: String, rawArtist: String): Pair<String, String> {
        var title = rawTitle.trim()
        var artist = rawArtist.trim()

        // 1. Quitar sufijo "- Topic" del artista si existe
        artist = artist.replace(TOPIC_SUFFIX_REGEX, "").trim()

        val hasValidArtist = artist.isNotBlank() &&
                !artist.equals("Desconocido", ignoreCase = true) &&
                !artist.equals("Unknown", ignoreCase = true)

        // 2. Si el título tiene guion divisor (" - ", " – ", " — ")
        if (DASH_REGEX.containsMatchIn(title)) {
            val parts = title.split(DASH_REGEX, limit = 2)
            if (parts.size == 2) {
                val left = parts[0].trim()
                val right = parts[1].trim()

                if (!hasValidArtist || artist.contains("VEVO", ignoreCase = true) || artist.contains("Topic", ignoreCase = true)) {
                    artist = left
                    title = right
                } else {
                    // Si el artista ya está presente o coincide con el lado izquierdo
                    if (left.equals(artist, ignoreCase = true) ||
                        artist.contains(left, ignoreCase = true) ||
                        left.contains(artist, ignoreCase = true)
                    ) {
                        title = right
                    } else {
                        // En títulos tipo "Artista - Título"
                        title = right
                    }
                }
            }
        }

        // 3. Si el título aún empieza con el artista (ej: "Vacations - Fooling You" o "Vacations: Fooling You")
        if (artist.isNotBlank() && !artist.equals("Desconocido", ignoreCase = true)) {
            val escapedArtist = Regex.escape(artist)
            val prefixRegex = Regex("(?i)^$escapedArtist\\s*[-–—:]?\\s*")
            title = title.replace(prefixRegex, "").trim()
        }

        // 4. Limpieza de etiquetas entre corchetes [...] o paréntesis (...)
        // EXCEPCIÓN: Si contiene feat., ft., con (case-insensitive), NO borrar.
        title = BRACKET_REGEX.replace(title) { matchResult ->
            val inner = matchResult.groupValues[2].trim()
            if (FEAT_REGEX.containsMatchIn(inner)) {
                // Conservar porque contiene colaboración (feat., ft., con)
                matchResult.value
            } else if (inner.isBlank() || JUNK_KEYWORD_REGEX.containsMatchIn(inner)) {
                // Eliminar porque es etiqueta basura (Oficial, Video, Audio, Lyric, etc.)
                ""
            } else {
                // Mantener otros paréntesis informativos
                matchResult.value
            }
        }

        // 5. Limpieza de palabras basura adicionales con separadores finales
        for (pattern in STANDALONE_JUNK) {
            title = title.replace(pattern, "").trim()
        }

        // 6. Limpiar paréntesis o corchetes que hayan quedado vacíos
        title = title.replace(EMPTY_BRACKETS_REGEX, "")

        // 7. Normalizar espacios múltiples y recortar puntuación marginal
        title = title.replace(WHITESPACE_REGEX, " ")
            .trim(' ', '-', '–', '—', ':', '"', '“', '”', ',', ';', '|')

        val finalTitle = title.ifBlank { rawTitle.trim() }
        val finalArtist = artist.ifBlank { rawArtist.trim() }

        return Pair(finalTitle, finalArtist)
    }
}
