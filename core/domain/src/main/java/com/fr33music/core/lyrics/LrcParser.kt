package com.fr33music.core.lyrics

data class LyricLine(
    val timeMs: Long,
    val text: String
)

class LrcParser {
    companion object {
        private val TIME_TAG_REGEX = Regex("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]")
        private val WORD_TAG_REGEX = Regex("<\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?>")

        fun parseLrc(text: String?): List<LyricLine> {
            if (text.isNullOrBlank()) return emptyList()

            val lines = text.lines()
            val result = mutableListOf<LyricLine>()

            for (rawLine in lines) {
                val trimmed = rawLine.trim()
                if (trimmed.isEmpty()) continue

                val matches = TIME_TAG_REGEX.findAll(trimmed).toList()
                if (matches.isEmpty()) continue

                // Remover etiquetas de tiempo estándar [mm:ss.xx] y de palabra por palabra <mm:ss.xx>
                val cleanText = trimmed
                    .replace(TIME_TAG_REGEX, "")
                    .replace(WORD_TAG_REGEX, "")
                    .trim()

                if (cleanText.isEmpty()) continue

                for (match in matches) {
                    val min = match.groupValues[1].toLongOrNull() ?: 0L
                    val sec = match.groupValues[2].toLongOrNull() ?: 0L
                    val fracStr = match.groupValues[3]
                    val fracMs = when (fracStr.length) {
                        1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                        2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                        3 -> fracStr.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val timeMs = min * 60_000L + sec * 1_000L + fracMs
                    result.add(LyricLine(timeMs = timeMs, text = cleanText))
                }
            }

            return result.sortedBy { it.timeMs }
        }

        fun parse(lrcContent: String?): List<LyricLine> = parseLrc(lrcContent)
    }

    fun parseLrc(text: String?): List<LyricLine> = Companion.parseLrc(text)
    fun parse(lrcContent: String?): List<LyricLine> = Companion.parseLrc(lrcContent)
}
