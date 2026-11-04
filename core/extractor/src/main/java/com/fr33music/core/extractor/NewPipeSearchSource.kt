package com.fr33music.core.extractor

import android.util.Log
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException

class NewPipeSearchSource(
    private val downloader: NewPipeDownloader = NewPipeDownloader()
) : SearchSource {

    init {
        ensureInitialized(downloader)
    }

    override suspend fun search(
        query: String,
        page: Int
    ): Result<List<Track>> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext Result.failure(ExtractorError.EmptyQuery)
        }

        try {
            val service = ServiceList.YouTube
            val extractor = service.getSearchExtractor(trimmedQuery, emptyList(), "")
            try {
                extractor.fetchPage()
            } catch (e: Exception) {
                Log.w(TAG, "Error en fetchPage de search para '$trimmedQuery': ${e.message}")
            }

            val initialItems = try { extractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()
            val tracks = mutableListOf<Track>()

            for (item in initialItems) {
                if (item is StreamInfoItem) {
                    val url = item.url ?: continue
                    val id = extractVideoId(url)
                    if (id.isBlank()) continue
                    val rawThumb = try {
                        item.thumbnails?.maxByOrNull { it.height }?.url
                            ?: item.thumbnails?.firstOrNull()?.url
                    } catch (_: Exception) { null }
                    val thumbUrl = Track.upgradeThumbnail(rawThumb)
                    val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                    val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: "Artista Desconocido"
                    val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                    tracks.add(
                        Track(
                            trackId = id,
                            title = title,
                            artistName = artist,
                            durationMs = duration,
                            thumbnailUrl = thumbUrl,
                            albumName = artist
                        )
                    )
                }
            }

            Result.success(tracks)
        } catch (e: IOException) {
            Result.failure(ExtractorError.NetworkError(e.message, e))
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }

    override suspend fun searchLookingDetails(query: String): Result<LookingSearchResult> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext Result.failure(ExtractorError.EmptyQuery)
        }

        try {
            val service = ServiceList.YouTube
            val extractor = service.getSearchExtractor(trimmedQuery, emptyList(), "")
            try {
                extractor.fetchPage()
            } catch (e: Exception) {
                Log.w(TAG, "Error en fetchPage de searchLookingDetails para '$trimmedQuery': ${e.message}")
            }

            val items = try { extractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()

            val channels = mutableListOf<ChannelCard>()
            val videos = mutableListOf<Track>()
            val playlists = mutableListOf<AlbumCard>()
            val seenChannelIds = mutableSetOf<String>()
            val seenVideoIds = mutableSetOf<String>()
            val seenPlaylistUrls = mutableSetOf<String>()

            // 1. Separar por tipo de resultado
            for (item in items) {
                when (item) {
                    is ChannelInfoItem -> {
                        val url = item.url ?: ""
                        val name = item.name?.takeIf { it.isNotBlank() } ?: "Canal"
                        val channelId = if (url.contains("/channel/")) {
                            url.substringAfter("/channel/").substringBefore("?").substringBefore("/")
                        } else if (url.isNotBlank()) {
                            url.substringAfterLast("/")
                        } else {
                            name
                        }
                        if (channelId.isNotBlank() && seenChannelIds.add(channelId)) {
                            val rawThumb = try {
                                item.thumbnails?.maxByOrNull { it.height }?.url
                                    ?: item.thumbnails?.firstOrNull()?.url
                            } catch (_: Exception) { null }
                            val thumb = Track.upgradeThumbnail(rawThumb)
                            val subCount = try { item.subscriberCount } catch (_: Exception) { 0L }
                            val desc = try { item.description } catch (_: Exception) { null }
                            channels.add(
                                ChannelCard(
                                    name = name,
                                    avatarUrl = thumb,
                                    subscriberCount = subCount,
                                    description = desc,
                                    url = url,
                                    channelId = channelId
                                )
                            )
                        }
                    }

                    is PlaylistInfoItem -> {
                        val url = item.url ?: ""
                        if (url.isNotBlank() && seenPlaylistUrls.add(url)) {
                            val title = item.name?.takeIf { it.isNotBlank() } ?: "Playlist"
                            val uploader = item.uploaderName?.takeIf { it.isNotBlank() } ?: "YouTube"
                            val rawThumb = try {
                                item.thumbnails?.maxByOrNull { it.height }?.url
                                    ?: item.thumbnails?.firstOrNull()?.url
                            } catch (_: Exception) { null }
                            val thumb = Track.upgradeThumbnail(rawThumb)
                            val streamCount = try { item.streamCount } catch (_: Exception) { 0L }
                            val uploaderUrl = try { item.uploaderUrl } catch (_: Exception) { null }
                            playlists.add(
                                AlbumCard(
                                    title = title,
                                    artistName = uploader,
                                    thumbnailUrl = thumb,
                                    songCount = streamCount,
                                    url = url,
                                    channelUrl = uploaderUrl
                                )
                            )
                        }
                    }

                    is StreamInfoItem -> {
                        val url = item.url ?: continue
                        val id = extractVideoId(url)
                        if (id.isNotBlank() && seenVideoIds.add(id)) {
                            val rawThumb = try {
                                item.thumbnails?.maxByOrNull { it.height }?.url
                                    ?: item.thumbnails?.firstOrNull()?.url
                            } catch (_: Exception) { null }
                            val thumb = Track.upgradeThumbnail(rawThumb)
                            val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                            val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: "Desconocido"
                            val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                            videos.add(
                                Track(
                                    trackId = id,
                                    title = title,
                                    artistName = artist,
                                    durationMs = duration,
                                    thumbnailUrl = thumb,
                                    albumName = artist
                                )
                            )
                        }
                    }
                }
            }

            // 2. Búsqueda enfocada a Canales si YouTube no devolvió el canal o falta coincidencia directa
            val hasExactChannel = channels.any { it.name.trim().equals(trimmedQuery, ignoreCase = true) }
            if (channels.isEmpty() || !hasExactChannel) {
                try {
                    val channelSearchExtractor = service.getSearchExtractor(
                        trimmedQuery,
                        listOf(YoutubeSearchQueryHandlerFactory.CHANNELS),
                        ""
                    )
                    channelSearchExtractor.fetchPage()
                    val channelItems = try { channelSearchExtractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()
                    for (item in channelItems) {
                        if (item is ChannelInfoItem) {
                            val url = item.url ?: ""
                            val name = item.name?.takeIf { it.isNotBlank() } ?: "Canal"
                            val channelId = if (url.contains("/channel/")) {
                                url.substringAfter("/channel/").substringBefore("?").substringBefore("/")
                            } else if (url.isNotBlank()) {
                                url.substringAfterLast("/")
                            } else {
                                name
                            }
                            if (channelId.isNotBlank() && seenChannelIds.add(channelId)) {
                                val rawThumb = try {
                                    item.thumbnails?.maxByOrNull { it.height }?.url
                                        ?: item.thumbnails?.firstOrNull()?.url
                                } catch (_: Exception) { null }
                                val thumb = Track.upgradeThumbnail(rawThumb)
                                val subCount = try { item.subscriberCount } catch (_: Exception) { 0L }
                                val desc = try { item.description } catch (_: Exception) { null }
                                val card = ChannelCard(
                                    name = name,
                                    avatarUrl = thumb,
                                    subscriberCount = subCount,
                                    description = desc,
                                    url = url,
                                    channelId = channelId
                                )
                                channels.add(card)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error en búsqueda secundaria de canales para '$trimmedQuery': ${e.message}")
                }
            }

            // Ordenar canales estrictamente por cantidad de suscriptores (de mayor a menor)
            val sortedChannels = channels.sortedByDescending { it.subscriberCount }.take(8)

            Result.success(
                LookingSearchResult(
                    channels = sortedChannels,
                    videos = videos,
                    playlists = playlists
                )
            )
        } catch (e: IOException) {
            Result.failure(ExtractorError.NetworkError(e.message, e))
        } catch (e: Exception) {
            Result.failure(ExtractorError.Unknown(e.message, e))
        }
    }

    override suspend fun getChannelVideos(channel: ChannelCard): Result<List<Track>> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val seenTrackIds = mutableSetOf<String>()
        if (channel.url.isBlank()) {
            return@withContext Result.success(tracks)
        }

        try {
            val service = ServiceList.YouTube
            val channelExtractor = service.getChannelExtractor(channel.url)
            try {
                channelExtractor.fetchPage()
            } catch (e: Exception) {
                Log.w(TAG, "Fallo al obtener página inicial del canal ${channel.name}: ${e.message}")
            }

            val tabs = try { channelExtractor.tabs } catch (_: Exception) { emptyList() }
            val videoTab = tabs.firstOrNull { tab ->
                try {
                    tab.contentFilters?.any { it.equals("videos", ignoreCase = true) } == true
                } catch (_: Exception) { false }
            } ?: tabs.firstOrNull()

            if (videoTab != null) {
                try {
                    val tabExtractor = service.getChannelTabExtractor(videoTab)
                    tabExtractor.fetchPage()

                    var currentPage = try { tabExtractor.initialPage } catch (_: Exception) { null }
                    var pageCount = 0
                    val maxPages = 4 // ~120 videos

                    while (currentPage != null && pageCount < maxPages) {
                        val items = try { currentPage.items } catch (_: Exception) { null } ?: emptyList()
                        for (item in items) {
                            if (item is StreamInfoItem) {
                                val url = item.url ?: continue
                                val id = extractVideoId(url)
                                if (id.isNotBlank() && seenTrackIds.add(id)) {
                                    val rawThumb = try {
                                        item.thumbnails?.maxByOrNull { it.height }?.url
                                            ?: item.thumbnails?.firstOrNull()?.url
                                    } catch (_: Exception) { null }
                                    val thumb = Track.upgradeThumbnail(rawThumb)
                                    val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                                    val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: channel.name
                                    val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                                    tracks.add(
                                        Track(
                                            trackId = id,
                                            title = title,
                                            artistName = artist,
                                            durationMs = duration,
                                            thumbnailUrl = thumb,
                                            albumName = channel.name,
                                            artistThumbnailUrl = channel.avatarUrl
                                        )
                                    )
                                }
                            }
                        }

                        if (currentPage.hasNextPage() && currentPage.nextPage != null) {
                            try {
                                currentPage = tabExtractor.getPage(currentPage.nextPage)
                                pageCount++
                            } catch (_: Exception) {
                                break
                            }
                        } else break
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error extrayendo pestaña de videos del canal ${channel.name}: ${e.message}")
                }
            }

            // Fallback si no vinieron videos de las pestañas
            if (tracks.isEmpty()) {
                val searchRes = search(channel.name)
                val fallbackTracks = searchRes.getOrElse { emptyList() }.filter {
                    it.artistName.equals(channel.name, ignoreCase = true)
                }
                tracks.addAll(fallbackTracks)
            }

            Result.success(tracks)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener videos de canal ${channel.name}: ${e.message}", e)
            val searchRes = search(channel.name)
            Result.success(searchRes.getOrElse { emptyList() })
        }
    }

    override suspend fun getAlbumTracks(album: AlbumCard): Result<List<Track>> = withContext(Dispatchers.IO) {
        try {
            val url = album.url
            if (!url.isNullOrBlank()) {
                // Si la URL apunta a una canción individual (sencillo)
                if (url.contains("watch?v=") && !url.contains("list=")) {
                    val id = extractVideoId(url)
                    if (id.isNotBlank()) {
                        var duration = album.durationMs
                        if (duration <= 0L) {
                            try {
                                val service = ServiceList.YouTube
                                val streamExtractor = service.getStreamExtractor(url)
                                streamExtractor.fetchPage()
                                val sec = streamExtractor.length
                                if (sec > 0) duration = sec * 1000L
                            } catch (e: Exception) {
                                Log.w(TAG, "No se pudo obtener duración precisa de $id: ${e.message}")
                            }
                        }
                        return@withContext Result.success(listOf(
                            Track(
                                trackId = id,
                                title = album.title,
                                artistName = album.artistName,
                                albumName = album.title,
                                durationMs = duration,
                                thumbnailUrl = album.thumbnailUrl
                            )
                        ))
                    }
                }

                val service = ServiceList.YouTube
                val extractor = service.getPlaylistExtractor(url)
                try {
                    extractor.fetchPage()
                } catch (e: Exception) {
                    Log.w(TAG, "Error en fetchPage de playlist: ${e.message}")
                }

                val tracks = mutableListOf<Track>()
                val items = try { extractor.initialPage?.items } catch (_: Exception) { null } ?: emptyList()
                for (item in items) {
                    if (item is StreamInfoItem) {
                        val id = extractVideoId(item.url)
                        val rawThumb = try {
                            item.thumbnails?.maxByOrNull { it.height }?.url
                                ?: item.thumbnails?.firstOrNull()?.url
                                ?: album.thumbnailUrl
                        } catch (_: Exception) { album.thumbnailUrl }
                        val thumbUrl = Track.upgradeThumbnail(rawThumb)
                        val title = item.name?.takeIf { it.isNotBlank() } ?: "Desconocido"
                        val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: album.artistName
                        val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
                        val track = Track(
                            trackId = id,
                            title = title,
                            artistName = artist,
                            albumName = album.title,
                            durationMs = duration,
                            thumbnailUrl = thumbUrl
                        )
                        tracks.add(track)
                    }
                }
                if (tracks.isNotEmpty()) {
                    return@withContext Result.success(tracks)
                }
            }

            // Fallback si no hay URL o lista vacía: buscar las canciones de ese álbum por texto
            val query = "${album.artistName} ${album.title}"
            val searchRes = search(query)
            val fallbackTracks = searchRes.getOrElse { emptyList() }.map {
                it.copy(albumName = album.title)
            }
            Result.success(fallbackTracks)
        } catch (e: Exception) {
            Log.w(TAG, "Fallo al obtener pistas de playlist: ${e.message}, intentando fallback por texto")
            val query = "${album.artistName} ${album.title}"
            val searchRes = search(query)
            val fallbackTracks = searchRes.getOrElse { emptyList() }.map {
                it.copy(albumName = album.title)
            }
            Result.success(fallbackTracks)
        }
    }

    private fun extractVideoId(url: String?): String {
        if (url.isNullOrBlank()) return ""
        return try {
            if (url.contains("v=")) {
                url.substringAfter("v=").substringBefore("&")
            } else if (url.contains("youtu.be/")) {
                url.substringAfter("youtu.be/").substringBefore("?")
            } else {
                url
            }
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        private const val TAG = "NewPipeSearchSource"
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
