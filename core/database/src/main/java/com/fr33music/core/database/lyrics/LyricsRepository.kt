package com.fr33music.core.database.lyrics

import com.fr33music.core.database.dao.LyricsDao
import com.fr33music.core.database.entity.LyricsEntity
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    suspend fun getLyrics(track: Track): String = withContext(Dispatchers.IO) {
        // 1. Revisar caché local en Room
        val cached = lyricsDao.getLyricsSync(track.trackId)
        if (cached != null && cached.plainText.isNotBlank()) {
            return@withContext cached.plainText
        }

        val cleanTitle = cleanTrackTitle(track.title)
        val cleanArtist = cleanArtistName(track.artistName)

        // 2. Intentar con LRCLIB
        val lrclibLyrics = fetchFromLrclib(cleanArtist, cleanTitle)
        if (!lrclibLyrics.isNullOrBlank()) {
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.trackId,
                    plainText = lrclibLyrics,
                    source = "LRCLIB"
                )
            )
            return@withContext lrclibLyrics
        }

        // 3. Fallback a lyrics.ovh
        val ovhLyrics = fetchFromLyricsOvh(cleanArtist, cleanTitle)
        if (!ovhLyrics.isNullOrBlank()) {
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.trackId,
                    plainText = ovhLyrics,
                    source = "lyrics.ovh"
                )
            )
            return@withContext ovhLyrics
        }

        "Letra no disponible"
    }

    private fun fetchFromLrclib(artist: String, title: String): String? {
        return try {
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val url = "https://lrclib.net/api/get?artist_name=$encodedArtist&track_name=$encodedTitle"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Fr33Music/1.0 (Android)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.body == null) return null

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) return null

            val jsonElement = json.parseToJsonElement(bodyString).jsonObject
            val plainLyrics = jsonElement["plainLyrics"]?.jsonPrimitive?.content
            if (!plainLyrics.isNullOrBlank()) {
                return plainLyrics
            }

            // Si solo hay syncedLyrics, quitar las marcas de tiempo para obtener texto plano
            val syncedLyrics = jsonElement["syncedLyrics"]?.jsonPrimitive?.content
            if (!syncedLyrics.isNullOrBlank()) {
                return syncedLyrics.replace(Regex("\\[\\d{2}:\\d{2}\\.\\d{2,3}\\]\\s*"), "").trim()
            }

            null
        } catch (_: Exception) {
            null
        }
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
