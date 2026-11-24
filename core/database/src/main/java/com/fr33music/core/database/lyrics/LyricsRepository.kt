package com.fr33music.core.database.lyrics

import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.entity.LyricsEntity
import com.fr33music.core.domain.model.LyricsResult
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    private val lyricsDao: LyricsDao,
    private val okHttpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getLyrics(track: Track): LyricsResult = withContext(Dispatchers.IO) {
        // 1. Revisar caché local en Room
        val cached = lyricsDao.getLyricsSync(track.trackId)
        if (cached != null && cached.plainText.isNotBlank()) {
            if (cached.syncedText != null) {
                return@withContext LyricsResult(
                    plainText = cached.plainText,
                    syncedText = cached.syncedText
                )
            }
            // Si tiene plainText pero no syncedText (de versiones anteriores), intentar consultar LRCLIB
            val cleanTitle = cleanTrackTitle(track.title)
            val cleanArtist = cleanArtistName(track.artistName)
            val lrclibLyrics = fetchFromLrclib(cleanArtist, cleanTitle)
            if (lrclibLyrics != null) {
                val finalPlain = if (lrclibLyrics.plainText.isNotBlank()) lrclibLyrics.plainText else cached.plainText
                val finalSynced = lrclibLyrics.syncedText
                lyricsDao.insertLyrics(
                    LyricsEntity(
                        trackId = track.trackId,
                        plainText = finalPlain,
                        syncedText = finalSynced,
                        source = "LRCLIB"
                    )
                )
                return@withContext LyricsResult(
                    plainText = finalPlain,
                    syncedText = finalSynced
                )
            }
            // Si no hay respuesta de red o no existe en LRCLIB, devolver caché existente
            return@withContext LyricsResult(
                plainText = cached.plainText,
                syncedText = null
            )
        }

        val cleanTitle = cleanTrackTitle(track.title)
        val cleanArtist = cleanArtistName(track.artistName)

        // 2. Intentar con LRCLIB (plainLyrics y syncedLyrics con validación de duración)
        val lrclibLyrics = fetchFromLrclib(cleanArtist, cleanTitle, track.durationMs)
        if (lrclibLyrics != null && (lrclibLyrics.plainText.isNotBlank() || !lrclibLyrics.syncedText.isNullOrBlank())) {
            val plain = if (lrclibLyrics.plainText.isNotBlank()) {
                lrclibLyrics.plainText
            } else {
                lrclibLyrics.syncedText?.let { cleanLrcTimestamps(it) }.orEmpty()
            }
            val finalPlain = plain.ifBlank { "Letra no disponible" }
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.trackId,
                    plainText = finalPlain,
                    syncedText = lrclibLyrics.syncedText,
                    source = "LRCLIB"
                )
            )
            return@withContext LyricsResult(
                plainText = finalPlain,
                syncedText = lrclibLyrics.syncedText
            )
        }

        // 3. Fallback a lyrics.ovh
        val ovhLyrics = fetchFromLyricsOvh(cleanArtist, cleanTitle)
        if (!ovhLyrics.isNullOrBlank()) {
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.trackId,
                    plainText = ovhLyrics,
                    syncedText = null,
                    source = "lyrics.ovh"
                )
            )
            return@withContext LyricsResult(
                plainText = ovhLyrics,
                syncedText = null
            )
        }

        LyricsResult(
            plainText = "Letra no disponible",
            syncedText = null
        )
    }

    suspend fun getPlainText(track: Track): String = getLyrics(track).plainText

    suspend fun translateLines(lines: List<String>, targetLang: String = "es"): List<String>? = withContext(Dispatchers.IO) {
        if (lines.isEmpty()) return@withContext emptyList()
        try {
            val textToTranslate = lines.joinToString("\n")
            val requestBody = FormBody.Builder()
                .add("q", textToTranslate)
                .build()

            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$targetLang&dt=t"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return@withContext null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return@withContext null

            val element = json.parseToJsonElement(bodyString)
            if (element !is JsonArray || element.isEmpty()) return@withContext null

            val segments = element[0]
            if (segments !is JsonArray) return@withContext null

            val fullTranslated = buildString {
                for (seg in segments) {
                    if (seg is JsonArray && seg.isNotEmpty()) {
                        val text = seg[0].jsonPrimitive.content
                        append(text)
                    }
                }
            }

            val translatedLines = fullTranslated.lines().map { it.trim() }
            translatedLines
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromLrclib(artist: String, title: String, trackDurationMs: Long = 0L): LyricsResult? {
        val cleanArtist = cleanArtistName(artist)
        val candidateTitles = buildTitleCandidates(cleanArtist, title)
        val targetDurationSec = (trackDurationMs / 1000L).coerceAtLeast(0L)

        // 1. Intentar /api/get con cada candidato de título (priorizando syncedLyrics con validación de duración)
        for (candidate in candidateTitles) {
            val result = fetchLrclibGet(cleanArtist, candidate, targetDurationSec)
            if (result != null && !result.syncedText.isNullOrBlank()) {
                return result
            }
        }

        // 2. Intentar /api/search con track_name y artist_name específicos
        for (candidate in candidateTitles) {
            val encodedArtist = URLEncoder.encode(cleanArtist, StandardCharsets.UTF_8.toString())
            val encodedCandidate = URLEncoder.encode(candidate, StandardCharsets.UTF_8.toString())
            val result = fetchLrclibSearch("track_name=$encodedCandidate&artist_name=$encodedArtist", targetDurationSec)
            if (result != null && !result.syncedText.isNullOrBlank()) {
                return result
            }
        }

        // 3. Fallback de búsqueda general con /api/search?q=...
        val q = "$cleanArtist ${candidateTitles.firstOrNull() ?: title}".trim()
        val encodedQ = URLEncoder.encode(q, StandardCharsets.UTF_8.toString())
        val searchResult = fetchLrclibSearch("q=$encodedQ", targetDurationSec)
        if (searchResult != null && !searchResult.syncedText.isNullOrBlank()) {
            return searchResult
        }

        // 4. Si ninguna tiene syncedLyrics, aceptar la que tenga al menos texto plano
        for (candidate in candidateTitles) {
            val result = fetchLrclibGet(cleanArtist, candidate, targetDurationSec)
            if (result != null && result.plainText.isNotBlank()) {
                return result
            }
        }
        if (searchResult != null && searchResult.plainText.isNotBlank()) {
            return searchResult
        }

        return null
    }

    private fun buildTitleCandidates(artist: String, title: String): List<String> {
        val candidates = mutableListOf<String>()
        val clean = cleanTrackTitle(title)
        if (clean.isNotBlank()) {
            candidates.add(clean)
        }

        if (clean.contains(" - ")) {
            val afterDash = clean.substringAfter(" - ").trim()
            if (afterDash.isNotBlank() && !candidates.contains(afterDash)) {
                candidates.add(afterDash)
            }
        }

        if (clean.startsWith(artist, ignoreCase = true)) {
            val stripped = clean.substring(artist.length).trim().removePrefix("-").trim()
            if (stripped.isNotBlank() && !candidates.contains(stripped)) {
                candidates.add(stripped)
            }
        }

        return candidates
    }

    private fun fetchLrclibGet(artist: String, title: String, targetDurationSec: Long = 0L): LyricsResult? {
        return try {
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val durationParam = if (targetDurationSec > 0) "&duration=$targetDurationSec" else ""
            val url = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle$durationParam"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Fr33Music/1.0 (Android)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return null

            val jsonElement = json.parseToJsonElement(bodyString).jsonObject

            // Validar que la duración no difiera en más de 4 segundos de la canción real
            if (targetDurationSec > 0) {
                val apiDuration = jsonElement["duration"]?.jsonPrimitive?.content?.toDoubleOrNull()
                if (apiDuration != null && kotlin.math.abs(apiDuration - targetDurationSec) > 4.0) {
                    return null
                }
            }

            val plainLyrics = jsonElement["plainLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            val syncedLyrics = jsonElement["syncedLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

            if (plainLyrics == null && syncedLyrics == null) return null

            val effectivePlain = plainLyrics ?: syncedLyrics?.let { cleanLrcTimestamps(it) }.orEmpty()

            LyricsResult(
                plainText = effectivePlain,
                syncedText = syncedLyrics
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchLrclibSearch(queryString: String, targetDurationSec: Long = 0L): LyricsResult? {
        return try {
            val url = "https://lrclib.net/api/search?$queryString"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Fr33Music/1.0 (Android)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return null

            val element = json.parseToJsonElement(bodyString)
            if (element !is JsonArray) return null

            var fallbackPlain: String? = null

            for (item in element) {
                val obj = item.jsonObject

                // Validar que la duración no difiera en más de 4 segundos de la canción real
                if (targetDurationSec > 0) {
                    val apiDuration = obj["duration"]?.jsonPrimitive?.content?.toDoubleOrNull()
                    if (apiDuration != null && kotlin.math.abs(apiDuration - targetDurationSec) > 4.0) {
                        continue
                    }
                }

                val syncedLyrics = obj["syncedLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                val plainLyrics = obj["plainLyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }

                if (syncedLyrics != null) {
                    val effectivePlain = plainLyrics ?: cleanLrcTimestamps(syncedLyrics)
                    return LyricsResult(
                        plainText = effectivePlain,
                        syncedText = syncedLyrics
                    )
                }

                if (fallbackPlain == null && plainLyrics != null) {
                    fallbackPlain = plainLyrics
                }
            }

            if (fallbackPlain != null) {
                LyricsResult(plainText = fallbackPlain, syncedText = null)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanLrcTimestamps(lrcContent: String): String {
        return lrcContent
            .replace(Regex("\\[\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?\\]\\s*"), "")
            .replace(Regex("<\\d{1,2}:\\d{2}(?:[.:]\\d{1,3})?>"), "")
            .trim()
    }

    private fun fetchFromLyricsOvh(artist: String, title: String): String? {
        return try {
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val url = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Fr33Music/1.0 (Android)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return null

            val jsonElement = json.parseToJsonElement(bodyString).jsonObject
            jsonElement["lyrics"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanTrackTitle(title: String): String {
        return title
            .replace(Regex("(?i)\\b(official\\s*video|official\\s*audio|official\\s*music\\s*video|video\\s*oficial|audio\\s*oficial|lyric\\s*video|letra)\\b"), "")
            .replace(Regex("(?i)\\[.*?\\]|\\(.*?\\)"), "")
            .replace(Regex("(?i)\\b(ft\\.|feat\\.|featuring).*"), "")
            .trim()
    }

    private fun cleanArtistName(artist: String): String {
        return artist
            .replace(Regex("(?i)\\s*-\\s*topic$"), "")
            .replace(Regex("(?i)\\b(vevo)\\b"), "")
            .trim()
    }
}
