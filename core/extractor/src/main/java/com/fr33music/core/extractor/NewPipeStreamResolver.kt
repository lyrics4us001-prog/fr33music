package com.fr33music.core.extractor

import android.content.Context
import android.net.Uri
import android.util.Log
import com.fr33music.core.domain.model.AudioFormat
import com.fr33music.core.domain.model.AudioStream
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.OfflineAudioSource
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.io.File
import java.io.IOException

class NewPipeStreamResolver(
    private val downloader: NewPipeDownloader = NewPipeDownloader(),
    private val offlineAudioSource: OfflineAudioSource? = null,
    private val context: Context? = null
) : StreamResolver {

    init {
        NewPipeSearchSource.ensureInitialized(downloader)
    }

    override suspend fun resolveAudio(track: Track): Result<AudioStream> = withContext(Dispatchers.IO) {
        try {
            val cleanId = extractVideoId(track.trackId)

            // CRÍTICO: Comprobar primero si el archivo existe localmente en almacenamiento privado
            var localFile: File? = null
            val minSizeBytes = 100 * 1024L

            // 1. A través de offlineAudioSource si está disponible
            val sourcePath = offlineAudioSource?.getLocalAudioPath(cleanId)
                ?: offlineAudioSource?.getLocalAudioPath(track.trackId)
            if (!sourcePath.isNullOrBlank()) {
                val f = File(sourcePath)
                if (f.exists() && f.length() >= minSizeBytes) {
                    localFile = f
                }
            }

            // 2. Comprobación directa en context.filesDir/downloads/
            if (localFile == null && context != null) {
                val downloadsDir = File(context.filesDir, "downloads")
                val opus = File(downloadsDir, "$cleanId.opus")
                val m4a = File(downloadsDir, "$cleanId.m4a")
                if (opus.exists() && opus.length() >= minSizeBytes) {
                    localFile = opus
                } else if (m4a.exists() && m4a.length() >= minSizeBytes) {
                    localFile = m4a
                }
            }

            // Si el archivo local existe y tiene contenido válido (>= 100KB), devolver URI local y NO llamar a YouTube
            if (localFile != null && localFile.exists() && localFile.length() >= minSizeBytes) {
                val isOpus = localFile.name.endsWith(".opus", ignoreCase = true)
                val localUri = Uri.fromFile(localFile).toString()
                Log.i("StreamResolver", "Archivo offline detectado para $cleanId: $localUri (${localFile.length()} bytes). Omitiendo petición de red.")
                val offlineStream = AudioStream(
                    url = localUri,
                    headers = emptyMap(),
                    mime = if (isOpus) "audio/opus" else "audio/mp4",
                    bitrateKbps = 160,
                    format = if (isOpus) AudioFormat.OPUS else AudioFormat.M4A,
                    durationMs = track.durationMs
                )
                return@withContext Result.success(offlineStream)
            }

            val videoUrl = if (cleanId.startsWith("http")) {
                cleanId
            } else {
                "https://www.youtube.com/watch?v=$cleanId"
            }

            // Reintentos automáticos si el extractor o la red fallan
            var lastError: Throwable? = null
            for (attempt in 1..3) {
                try {
                    Log.d("StreamResolver", "Resolviendo stream en red desde YouTube para: $videoUrl (intento $attempt/3)")
                    val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
                    val audioStreams = streamInfo.audioStreams

                    if (audioStreams.isNullOrEmpty()) {
                        throw ExtractorError.StreamUnavailable("No se encontraron streams de audio para este contenido")
                    }

                    val candidates = audioStreams.map { stream ->
                        val format = AudioStreamSelector.parseFormat(
                            formatName = stream.format?.name ?: stream.format?.mimeType,
                            mimeType = stream.format?.mimeType
                        )
                        AudioCandidate(
                            url = stream.content ?: "",
                            mime = stream.format?.mimeType ?: "audio/*",
                            bitrateKbps = stream.averageBitrate.takeIf { it > 0 } ?: stream.bitrate,
                            format = format
                        )
                    }.filter { it.url.isNotEmpty() }

                    val bestCandidate = AudioStreamSelector.selectBestStream(candidates)
                        ?: throw ExtractorError.StreamUnavailable("No se encontró stream compatible (OPUS/M4A)")

                    val headers = mutableMapOf<String, String>(
                        "User-Agent" to NewPipeDownloader.USER_AGENT
                    )

                    val cookieMap = downloader.getAllCookies()
                    if (cookieMap.isNotEmpty()) {
                        headers["Cookie"] = cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
                    }

                    val resolvedDuration = try {
                        if (streamInfo.duration > 0) streamInfo.duration * 1000L else track.durationMs
                    } catch (_: Exception) {
                        track.durationMs
                    }

                    val resolved = AudioStream(
                        url = bestCandidate.url,
                        headers = headers,
                        mime = bestCandidate.mime,
                        bitrateKbps = bestCandidate.bitrateKbps,
                        format = bestCandidate.format,
                        durationMs = resolvedDuration
                    )

                    return@withContext Result.success(resolved)
                } catch (e: Exception) {
                    val fullErrorText = (e.message ?: "") + " " + (e.cause?.message ?: "")
                    if (fullErrorText.contains("age-restricted", ignoreCase = true) ||
                        fullErrorText.contains("sign in to confirm your age", ignoreCase = true) ||
                        fullErrorText.contains("confirm your age", ignoreCase = true)
                    ) {
                        Log.w("StreamResolver", "Video con restricción de edad detectado: $videoUrl")
                        return@withContext Result.failure(ExtractorError.AgeRestricted(cause = e))
                    }
                    lastError = e
                    Log.w("StreamResolver", "Fallo al resolver stream en intento $attempt/3: ${e.message}")
                    if (attempt < 3) {
                        delay(500L * attempt)
                    }
                }
            }

            val finalError = lastError ?: Exception("Error desconocido al resolver audio")
            val finalErrorText = (finalError.message ?: "") + " " + (finalError.cause?.message ?: "")
            if (finalErrorText.contains("age-restricted", ignoreCase = true) ||
                finalErrorText.contains("sign in to confirm your age", ignoreCase = true) ||
                finalErrorText.contains("confirm your age", ignoreCase = true)
            ) {
                Result.failure(ExtractorError.AgeRestricted(cause = finalError))
            } else if (finalError is IOException) {
                Result.failure(ExtractorError.NetworkError(finalError.message, finalError))
            } else if (finalError is ExtractorError) {
                Result.failure(finalError)
            } else {
                Result.failure(ExtractorError.Unknown(finalError.message, finalError))
            }
        } catch (e: Exception) {
            val fullMsg = (e.message ?: "") + " " + (e.cause?.message ?: "")
            if (fullMsg.contains("age-restricted", ignoreCase = true) ||
                fullMsg.contains("sign in to confirm your age", ignoreCase = true) ||
                fullMsg.contains("confirm your age", ignoreCase = true)
            ) {
                Result.failure(ExtractorError.AgeRestricted(cause = e))
            } else {
                Result.failure(ExtractorError.Unknown(e.message, e))
            }
        }
    }

    private fun extractVideoId(urlOrId: String): String {
        return when {
            urlOrId.contains("v=") -> urlOrId.substringAfter("v=").substringBefore("&")
            urlOrId.contains("youtu.be/") -> urlOrId.substringAfter("youtu.be/").substringBefore("?")
            else -> urlOrId.trim()
        }
    }
}
