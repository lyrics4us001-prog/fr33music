package com.fr33music.core.extractor

import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException

class NewPipeSearchSource(
    private val downloader: NewPipeDownloader = NewPipeDownloader()
) : SearchSource {

    init {
        ensureInitialized(downloader)
    }

    override suspend fun search(query: String, page: Int): Result<List<Track>> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext Result.failure(ExtractorError.EmptyQuery)
        }

        try {
            val service = ServiceList.YouTube
            val extractor: SearchExtractor = service.getSearchExtractor(trimmedQuery)
            extractor.fetchPage()

            val initialPage = extractor.initialPage
            val tracks = mutableListOf<Track>()

            for (item in initialPage.items) {
                if (item is StreamInfoItem) {
                    val id = extractVideoId(item.url)
                    val thumbUrl = item.thumbnails?.maxByOrNull { it.height }?.url
                        ?: item.thumbnails?.firstOrNull()?.url
                    val track = Track(
                        trackId = id,
                        title = item.name ?: "Desconocido",
                        artistName = item.uploaderName ?: "Artista Desconocido",
                        durationMs = if (item.duration > 0) item.duration * 1000L else 0L,
                        thumbnailUrl = thumbUrl
                    )
                    tracks.add(track)
                }
            }

            Result.success(tracks)
        } catch (e: IOException) {
            Result.failure(ExtractorError.NetworkError(e.message, e))
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }

    private fun extractVideoId(url: String): String {
        return if (url.contains("v=")) {
            url.substringAfter("v=").substringBefore("&")
        } else if (url.contains("youtu.be/")) {
            url.substringAfter("youtu.be/").substringBefore("?")
        } else {
            url
        }
    }

    companion object {
        private var isInitialized = false

        @Synchronized
        fun ensureInitialized(downloader: NewPipeDownloader) {
            if (!isInitialized) {
                NewPipe.init(downloader)
                isInitialized = true
            }
        }
    }
}
