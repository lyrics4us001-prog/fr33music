package com.fr33music.core.extractor

import com.fr33music.core.domain.model.AudioStream
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import java.io.IOException

class NewPipeStreamResolver(
    private val downloader: NewPipeDownloader = NewPipeDownloader()
) : StreamResolver {

    init {
        NewPipeSearchSource.ensureInitialized(downloader)
    }

    override suspend fun resolveAudio(track: Track): Result<AudioStream> = withContext(Dispatchers.IO) {
        try {
            val videoUrl = if (track.trackId.startsWith("http")) {
                track.trackId
            } else {
                "https://www.youtube.com/watch?v=${track.trackId}"
            }

            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, videoUrl)
            val audioStreams = streamInfo.audioStreams

            if (audioStreams.isNullOrEmpty()) {
                return@withContext Result.failure(
                    ExtractorError.StreamUnavailable("No se encontraron streams de audio para este contenido")
                )
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
                ?: return@withContext Result.failure(
                    ExtractorError.StreamUnavailable("No se encontró stream compatible (OPUS/M4A)")
                )

            val headers = mutableMapOf<String, String>(
                "User-Agent" to NewPipeDownloader.USER_AGENT,
                "Referer" to NewPipeDownloader.REFERER_YOUTUBE
            )

            val cookieMap = downloader.getAllCookies()
            if (cookieMap.isNotEmpty()) {
                headers["Cookie"] = cookieMap.entries.joinToString("; ") { "${it.key}=${it.value}" }
            }

            val resolved = AudioStream(
                url = bestCandidate.url,
                headers = headers,
                mime = bestCandidate.mime,
                bitrateKbps = bestCandidate.bitrateKbps,
                format = bestCandidate.format
            )

            Result.success(resolved)
        } catch (e: IOException) {
            Result.failure(ExtractorError.NetworkError(e.message, e))
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }
}
