package com.fr33music.core.extractor

import android.util.Base64
import android.util.Log
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

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
                    val title = item.name?.takeIf { it.isNotBlank() && !it.startsWith("Pista ") } ?: "Desconocido"
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
                            val title = item.name?.takeIf { it.isNotBlank() && !it.startsWith("Pista ") } ?: "Desconocido"
                            val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: "Artista Desconocido"
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

    private val pageTokenCache = ConcurrentHashMap<String, Page>()

    private fun savePage(page: Page?): String? {
        if (page == null) return null
        val id = UUID.randomUUID().toString()
        pageTokenCache[id] = page
        if (pageTokenCache.size > 200) {
            val toRemove = pageTokenCache.keys.take(50)
            toRemove.forEach { pageTokenCache.remove(it) }
        }
        val base64 = try {
            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use { it.writeObject(page) }
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (_: Exception) { null }
        return if (base64 != null) "$id|$base64" else id
    }

    private fun getPage(token: String): Page? {
        val id = token.substringBefore("|")
        val cached = pageTokenCache[id]
        if (cached != null) return cached
        if (token.contains("|")) {
            val base64 = token.substringAfter("|")
            try {
                val bytes = Base64.decode(base64, Base64.NO_WRAP)
                ByteArrayInputStream(bytes).use { bais ->
                    ObjectInputStream(bais).use { ois ->
                        val obj = ois.readObject() as? Page
                        if (obj != null) {
                            pageTokenCache[id] = obj
                            return obj
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun mapStreamInfoItemToTrack(item: StreamInfoItem, channel: ChannelCard): Track? {
        val url = item.url ?: return null
        val id = extractVideoId(url)
        if (id.isBlank()) return null
        val rawThumb = try {
            item.thumbnails?.maxByOrNull { it.height }?.url
                ?: item.thumbnails?.firstOrNull()?.url
        } catch (_: Exception) { null }
        val thumb = Track.upgradeThumbnail(rawThumb)
        val title = item.name?.takeIf { it.isNotBlank() && !it.startsWith("Pista ") } ?: "Desconocido"
        val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: channel.name.takeIf { it.isNotBlank() } ?: "Artista Desconocido"
        val duration = try { if (item.duration > 0) item.duration * 1000L else 0L } catch (_: Exception) { 0L }
        return Track(
            trackId = id,
            title = title,
            artistName = artist,
            durationMs = duration,
            thumbnailUrl = thumb,
            albumName = channel.name,
            artistThumbnailUrl = channel.avatarUrl
        )
    }

    override suspend fun getChannelVideos(channel: ChannelCard): Result<List<Track>> {
        return getChannelVideosPaged(channel, null).map { it.tracks }
    }

    override suspend fun getChannelVideosPaged(
        channel: ChannelCard,
        nextPageToken: String?
    ): Result<ChannelVideosResult> = withContext(Dispatchers.IO) {
        if (channel.url.isBlank() && channel.channelId.isBlank()) {
            return@withContext Result.success(ChannelVideosResult(emptyList(), null))
        }

        try {
            val service = ServiceList.YouTube
            val tracks = mutableListOf<Track>()
            val seenTrackIds = mutableSetOf<String>()

            // 1. Resolver uploadsPlaylistId ("UU" + channelId sin prefijo "UC")
            var resolvedChannelId = channel.channelId
            if (!resolvedChannelId.startsWith("UC") && channel.url.contains("/channel/UC")) {
                resolvedChannelId = channel.url.substringAfter("/channel/").substringBefore("/").substringBefore("?")
            }

            if (!resolvedChannelId.startsWith("UC") && channel.url.isNotBlank()) {
                try {
                    val chExtractor = service.getChannelExtractor(channel.url)
                    chExtractor.fetchPage()
                    val id = chExtractor.id
                    if (!id.isNullOrBlank() && id.startsWith("UC")) {
                        resolvedChannelId = id
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo extraer id de canal para ${channel.name}: ${e.message}")
                }
            }

            val uploadsPlaylistId = if (resolvedChannelId.startsWith("UC")) {
                "UU" + resolvedChannelId.removePrefix("UC")
            } else null

            // Si tenemos nextPageToken, resolver la página siguiente
            if (!nextPageToken.isNullOrBlank()) {
                val page = getPage(nextPageToken)
                if (page != null) {
                    // Paginación vía playlist de subidas
                    if (uploadsPlaylistId != null) {
                        try {
                            val playlistUrl = "https://www.youtube.com/playlist?list=$uploadsPlaylistId"
                            val playlistExtractor = service.getPlaylistExtractor(playlistUrl)
                            val resultPage = playlistExtractor.getPage(page)
                            val items = resultPage?.items ?: emptyList()
                            for (item in items) {
                                if (item is StreamInfoItem) {
                                    val track = mapStreamInfoItemToTrack(item, channel)
                                    if (track != null && seenTrackIds.add(track.trackId)) {
                                        tracks.add(track)
                                    }
                                }
                            }
                            val nextToken = if (resultPage?.hasNextPage() == true && resultPage.nextPage != null) {
                                savePage(resultPage.nextPage)
                            } else null
                            return@withContext Result.success(ChannelVideosResult(tracks, nextToken))
                        } catch (e: Exception) {
                            Log.w(TAG, "Fallo al paginar por playlist de subidas: ${e.message}")
                        }
                    }

                    // Fallback para paginación vía tabExtractor si falla playlist
                    try {
                        val chExtractor = service.getChannelExtractor(channel.url)
                        chExtractor.fetchPage()
                        val videoTab = chExtractor.tabs.firstOrNull { tab ->
                            try {
                                tab.contentFilters?.any { it.equals("videos", ignoreCase = true) } == true
                            } catch (_: Exception) { false }
                        } ?: chExtractor.tabs.firstOrNull()

                        if (videoTab != null) {
                            val tabExtractor = service.getChannelTabExtractor(videoTab)
                            val resultPage = tabExtractor.getPage(page)
                            val items = resultPage?.items ?: emptyList()
                            for (item in items) {
                                if (item is StreamInfoItem) {
                                    val track = mapStreamInfoItemToTrack(item, channel)
                                    if (track != null && seenTrackIds.add(track.trackId)) {
                                        tracks.add(track)
                                    }
                                }
                            }
                            val nextToken = if (resultPage?.hasNextPage() == true && resultPage.nextPage != null) {
                                savePage(resultPage.nextPage)
                            } else null
                            return@withContext Result.success(ChannelVideosResult(tracks, nextToken))
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Fallo al paginar por pestaña de canal: ${e.message}")
                    }
                }
                return@withContext Result.success(ChannelVideosResult(emptyList(), null))
            }

            // Primera página: Intentar con uploadsPlaylist (UU...)
            if (uploadsPlaylistId != null) {
                try {
                    val playlistUrl = "https://www.youtube.com/playlist?list=$uploadsPlaylistId"
                    val playlistExtractor = service.getPlaylistExtractor(playlistUrl)
                    playlistExtractor.fetchPage()
                    val totalVideos = try {
                        val count = playlistExtractor.streamCount
                        if (count > 0) count else null
                    } catch (_: Exception) { null }
                    val initialPage = playlistExtractor.initialPage
                    val items = initialPage?.items ?: emptyList()
                    for (item in items) {
                        if (item is StreamInfoItem) {
                            val track = mapStreamInfoItemToTrack(item, channel)
                            if (track != null && seenTrackIds.add(track.trackId)) {
                                tracks.add(track)
                            }
                        }
                    }
                    if (tracks.isNotEmpty()) {
                        val nextToken = if (initialPage?.hasNextPage() == true && initialPage.nextPage != null) {
                            savePage(initialPage.nextPage)
                        } else null
                        return@withContext Result.success(ChannelVideosResult(tracks, nextToken, totalVideos))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo cargar playlist de subidas $uploadsPlaylistId: ${e.message}, intentando tabs")
                }
            }

            // Fallback: ChannelTabExtractor (pestaña videos)
            try {
                val channelExtractor = service.getChannelExtractor(channel.url)
                channelExtractor.fetchPage()
                val tabs = try { channelExtractor.tabs } catch (_: Exception) { emptyList() }
                val videoTab = tabs.firstOrNull { tab ->
                    try {
                        tab.contentFilters?.any { it.equals("videos", ignoreCase = true) } == true
                    } catch (_: Exception) { false }
                } ?: tabs.firstOrNull()

                if (videoTab != null) {
                    val tabExtractor = service.getChannelTabExtractor(videoTab)
                    tabExtractor.fetchPage()
                    val initialPage = tabExtractor.initialPage
                    val items = initialPage?.items ?: emptyList()
                    for (item in items) {
                        if (item is StreamInfoItem) {
                            val track = mapStreamInfoItemToTrack(item, channel)
                            if (track != null && seenTrackIds.add(track.trackId)) {
                                tracks.add(track)
                            }
                        }
                    }
                    val nextToken = if (initialPage?.hasNextPage() == true && initialPage.nextPage != null) {
                        savePage(initialPage.nextPage)
                    } else null
                    if (tracks.isNotEmpty()) {
                        return@withContext Result.success(ChannelVideosResult(tracks, nextToken))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error extrayendo pestaña de videos de ${channel.name}: ${e.message}")
            }

            // Fallback final: Búsqueda general por nombre del canal
            val searchRes = search(channel.name)
            val fallbackTracks = searchRes.getOrElse { emptyList() }.filter {
                it.artistName.equals(channel.name, ignoreCase = true)
            }
            Result.success(ChannelVideosResult(fallbackTracks, null))
        } catch (e: Exception) {
            Log.e(TAG, "Error en getChannelVideosPaged para ${channel.name}: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun searchChannelVideos(
        channel: ChannelCard,
        query: String,
        nextPageToken: String?
    ): Result<ChannelVideosResult> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext getChannelVideosPaged(channel, null)
        }

        try {
            val service = ServiceList.YouTube
            val tracks = mutableListOf<Track>()
            val seenTrackIds = mutableSetOf<String>()
            val fullQuery = "${channel.name} $trimmedQuery"

            if (!nextPageToken.isNullOrBlank()) {
                val page = getPage(nextPageToken)
                if (page != null) {
                    val searchExtractor = service.getSearchExtractor(
                        fullQuery,
                        listOf(YoutubeSearchQueryHandlerFactory.VIDEOS),
                        ""
                    )
                    val resultPage = searchExtractor.getPage(page)
                    val items = resultPage?.items ?: emptyList()
                    for (item in items) {
                        if (item is StreamInfoItem) {
                            val track = mapStreamInfoItemToTrack(item, channel)
                            if (track != null && seenTrackIds.add(track.trackId)) {
                                tracks.add(track)
                            }
                        }
                    }
                    val nextToken = if (resultPage?.hasNextPage() == true && resultPage.nextPage != null) {
                        savePage(resultPage.nextPage)
                    } else null
                    return@withContext Result.success(ChannelVideosResult(tracks, nextToken))
                }
                return@withContext Result.success(ChannelVideosResult(emptyList(), null))
            }

            // Primera página de búsqueda interna
            val searchExtractor = service.getSearchExtractor(
                fullQuery,
                listOf(YoutubeSearchQueryHandlerFactory.VIDEOS),
                ""
            )
            searchExtractor.fetchPage()
            val initialPage = searchExtractor.initialPage
            val items = initialPage?.items ?: emptyList()
            val allStreamItems = items.filterIsInstance<StreamInfoItem>()

            // Priorizar videos subidos directamente por este canal
            val channelMatches = allStreamItems.filter { item ->
                item.uploaderUrl?.contains(channel.channelId) == true ||
                item.uploaderName?.equals(channel.name, ignoreCase = true) == true
            }

            val itemsToUse = if (channelMatches.isNotEmpty()) channelMatches else allStreamItems
            for (item in itemsToUse) {
                val track = mapStreamInfoItemToTrack(item, channel)
                if (track != null && seenTrackIds.add(track.trackId)) {
                    tracks.add(track)
                }
            }

            val nextToken = if (initialPage?.hasNextPage() == true && initialPage.nextPage != null) {
                savePage(initialPage.nextPage)
            } else null

            Result.success(ChannelVideosResult(tracks, nextToken))
        } catch (e: Exception) {
            Log.e(TAG, "Error en searchChannelVideos para ${channel.name} con query '$query': ${e.message}", e)
            Result.failure(e)
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
                                title = album.title.takeIf { it.isNotBlank() && !it.startsWith("Pista ") } ?: "Desconocido",
                                artistName = album.artistName.takeIf { it.isNotBlank() } ?: "Artista Desconocido",
                                albumName = album.title,
                                durationMs = if (duration > 0) duration else 0L,
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
                        val title = item.name?.takeIf { it.isNotBlank() && !it.startsWith("Pista ") } ?: "Desconocido"
                        val artist = item.uploaderName?.takeIf { it.isNotBlank() } ?: album.artistName.takeIf { it.isNotBlank() } ?: "Artista Desconocido"
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

    override suspend fun getSuggestions(
        query: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success(emptyList())
        }
        try {
            val suggestions = ServiceList.YouTube.suggestionExtractor?.suggestionList(trimmed) ?: emptyList()
            Result.success(suggestions)
        } catch (e: Exception) {
            Log.w(TAG, "Error obteniendo sugerencias para '$trimmed': ${e.message}")
            Result.success(emptyList())
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
