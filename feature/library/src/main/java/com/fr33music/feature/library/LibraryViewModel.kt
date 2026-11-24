package com.fr33music.feature.library

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.PlaylistDao
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.PlaylistEntity
import com.fr33music.core.database.entity.PlaylistSongCrossRef
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.ArtistBackupDto
import com.fr33music.core.domain.model.FullLibraryBackupDto
import com.fr33music.core.domain.model.PlaylistBackupDto
import com.fr33music.core.domain.model.PlaylistExportDto
import com.fr33music.core.domain.model.Track
import com.fr33music.core.domain.model.TrackExportDto
import com.fr33music.core.extractor.StreamResolver
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

enum class LibraryTab(val title: String) {
    ARTISTS("Artistas"),
    PLAYLISTS("Playlists"),
    ALL_SONGS("Todas las canciones")
}

enum class LibrarySortOrder(val title: String) {
    RECENTLY_ADDED("Añadidos recientemente"),
    ALPHABETICAL("Orden alfabético")
}

data class ArtistItem(
    val name: String,
    val trackCount: Int,
    val thumbnailUrl: String?,
    val firstTrackId: String? = null,
    val latestTimestamp: Long = 0L
)

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.ARTISTS,
    val artists: List<ArtistItem> = emptyList(),
    val playlists: List<PlaylistWithSongCount> = emptyList(),
    val allSongs: List<TrackEntity> = emptyList(),
    val selectedArtist: String? = null,
    val selectedPlaylistId: Long? = null,
    val selectedPlaylistName: String? = null,
    val playlistSongs: List<TrackEntity> = emptyList(),
    val filteredTracks: List<TrackEntity> = emptyList(),
    val sortOrder: LibrarySortOrder = LibrarySortOrder.RECENTLY_ADDED
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val downloadRepository: DownloadRepository,
    private val playerRepository: PlayerRepository,
    private val libraryRepository: LibraryRepository,
    private val streamResolver: StreamResolver
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.ARTISTS)
    private val _selectedArtist = MutableStateFlow<String?>(null)
    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    private val _selectedPlaylistName = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(LibrarySortOrder.RECENTLY_ADDED)
    val sortOrder: StateFlow<LibrarySortOrder> = _sortOrder.asStateFlow()

    private data class FilterSelection(
        val tab: LibraryTab = LibraryTab.ARTISTS,
        val artist: String? = null,
        val playlistId: Long? = null,
        val playlistName: String? = null,
        val sortOrder: LibrarySortOrder = LibrarySortOrder.RECENTLY_ADDED
    )

    private val filterSelection = combine(
        _selectedTab,
        _selectedArtist,
        _selectedPlaylistId,
        _selectedPlaylistName,
        _sortOrder
    ) { tab, artist, playlistId, playlistName, sortOrder ->
        FilterSelection(tab, artist, playlistId, playlistName, sortOrder)
    }

    private val playlistSongsFlow = _selectedPlaylistId.flatMapLatest { playlistId ->
        if (playlistId != null) {
            libraryRepository.getSongsForPlaylist(playlistId)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        filterSelection,
        trackDao.getAllTracks(),
        artistDao.getAllArtists(),
        libraryRepository.getPlaylists(),
        playlistSongsFlow
    ) { filter, allTracks, artistEntities, playlistsList, playlistSongs ->
        val allLibraryTracks = allTracks

        // Lista de "Todas las canciones": ordenadas según sortOrder
        val allSongsSorted = when (filter.sortOrder) {
            LibrarySortOrder.RECENTLY_ADDED -> allLibraryTracks.sortedByDescending { it.timestamp }
            LibrarySortOrder.ALPHABETICAL -> allLibraryTracks.sortedWith(
                compareBy({ it.title.lowercase() }, { it.artistName.lowercase() })
            )
        }

        // Playlists: ordenadas según sortOrder
        val playlistsSorted = when (filter.sortOrder) {
            LibrarySortOrder.RECENTLY_ADDED -> playlistsList.sortedByDescending { it.createdAt }
            LibrarySortOrder.ALPHABETICAL -> playlistsList.sortedBy { it.name.lowercase() }
        }

        // Canciones de playlist: ordenadas según sortOrder
        val playlistSongsSorted = when (filter.sortOrder) {
            LibrarySortOrder.RECENTLY_ADDED -> playlistSongs
            LibrarySortOrder.ALPHABETICAL -> playlistSongs.sortedWith(
                compareBy({ it.artistName.lowercase() }, { it.title.lowercase() })
            )
        }

        // Agrupación por artista con fallback de miniatura y ordenamiento según sortOrder (filtrado a canciones guardadas/descargadas)
        val artistSourceTracks = allLibraryTracks.filter { it.isSaved || it.isDownloaded }
        val rawArtistItems = artistSourceTracks
            .groupBy { it.artistName }
            .map { (artistName, trackList) ->
                val entity = artistEntities.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
                val firstValidThumbTrack = trackList.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }
                val firstTrack = trackList.firstOrNull()
                val thumb = entity?.thumbnailUrl?.takeIf { it.isNotBlank() }
                    ?: firstValidThumbTrack?.thumbnailUrl?.takeIf { it.isNotBlank() }
                    ?: firstTrack?.let { "https://i.ytimg.com/vi/${it.id}/hqdefault.jpg" }
                val firstTrackId = firstValidThumbTrack?.id ?: firstTrack?.id
                val maxTimestamp = trackList.maxOfOrNull { it.timestamp } ?: 0L
                ArtistItem(
                    name = artistName,
                    trackCount = trackList.size,
                    thumbnailUrl = thumb,
                    firstTrackId = firstTrackId,
                    latestTimestamp = maxTimestamp
                )
            }

        val artistItems = when (filter.sortOrder) {
            LibrarySortOrder.RECENTLY_ADDED -> rawArtistItems.sortedByDescending { it.latestTimestamp }
            LibrarySortOrder.ALPHABETICAL -> rawArtistItems.sortedBy { it.name.lowercase() }
        }

        val filtered = when {
            filter.artist != null -> {
                val artistTracks = artistSourceTracks.filter { it.artistName.equals(filter.artist, ignoreCase = true) }
                when (filter.sortOrder) {
                    LibrarySortOrder.RECENTLY_ADDED -> artistTracks.sortedByDescending { it.timestamp }
                    LibrarySortOrder.ALPHABETICAL -> artistTracks.sortedWith(
                        compareBy({ it.title.lowercase() }, { it.artistName.lowercase() })
                    )
                }
            }
            else -> emptyList()
        }

        LibraryUiState(
            selectedTab = filter.tab,
            artists = artistItems,
            playlists = playlistsSorted,
            allSongs = allSongsSorted,
            selectedArtist = filter.artist,
            selectedPlaylistId = filter.playlistId,
            selectedPlaylistName = filter.playlistName,
            playlistSongs = playlistSongsSorted,
            filteredTracks = filtered,
            sortOrder = filter.sortOrder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun setSortOrder(order: LibrarySortOrder) {
        _sortOrder.value = order
    }

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
        _selectedArtist.value = null
        _selectedPlaylistId.value = null
        _selectedPlaylistName.value = null
    }

    fun selectArtist(artist: String?) {
        _selectedArtist.value = artist
    }

    fun selectPlaylist(playlistId: Long?, playlistName: String? = null) {
        _selectedPlaylistId.value = playlistId
        _selectedPlaylistName.value = playlistName
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            libraryRepository.createPlaylist(name)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            if (_selectedPlaylistId.value == id) {
                _selectedPlaylistId.value = null
                _selectedPlaylistName.value = null
            }
            libraryRepository.deletePlaylist(id)
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            libraryRepository.renamePlaylist(id, trimmed)
            if (_selectedPlaylistId.value == id) {
                _selectedPlaylistName.value = trimmed
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            libraryRepository.addSongToPlaylist(playlistId, track)
        }
    }

    fun addSongsToPlaylist(playlistId: Long, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            libraryRepository.addSongsToPlaylist(playlistId, tracks)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            libraryRepository.removeSongFromPlaylist(playlistId, trackId)
        }
    }

    fun refreshMetadata(tracks: List<TrackEntity>, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            for (track in tracks) {
                try {
                    val res = streamResolver.resolveMetadata(track.id)
                    res.onSuccess { meta ->
                        val cleanTitle = meta.title.takeIf { it.isNotBlank() && !it.startsWith("Pista ") && it != "Desconocido" } ?: track.title
                        val cleanArtist = meta.artistName.takeIf { it.isNotBlank() && it != "Desconocido" && it != "Artista Desconocido" } ?: track.artistName
                        val cleanDuration = if (meta.durationMs > 0) meta.durationMs else track.durationMs
                        val cleanThumb = if (!meta.thumbnailUrl.isNullOrBlank()) meta.thumbnailUrl else track.thumbnailUrl

                        trackDao.updateTrack(
                            track.copy(
                                title = cleanTitle,
                                artistName = cleanArtist,
                                durationMs = cleanDuration,
                                thumbnailUrl = cleanThumb
                            )
                        )

                        if (cleanArtist.isNotBlank() && cleanArtist != "Desconocido" && cleanArtist != "Artista Desconocido") {
                            val existingArtist = artistDao.getArtistSync(cleanArtist)
                            if (existingArtist == null && !cleanThumb.isNullOrBlank()) {
                                artistDao.insertArtist(ArtistEntity(name = cleanArtist, thumbnailUrl = cleanThumb))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w("LibraryViewModel", "Error al refrescar metadatos de ${track.id}: ${e.message}")
                }
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun playFromList(track: Track, list: List<Track>) {
        if (list.isEmpty()) return
        val startIndex = list.indexOfFirst { it.trackId == track.trackId }.coerceAtLeast(0)
        viewModelScope.launch {
            playerRepository.setQueue(list, startIndex)
        }
    }

    fun playAll(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, tracks.lastIndex)
        viewModelScope.launch {
            playerRepository.setQueue(tracks, validIndex)
        }
    }

    fun toggleSave(track: Track, isCurrentlySaved: Boolean) {
        viewModelScope.launch {
            libraryRepository.toggleSave(track, isCurrentlySaved)
        }
    }

    fun deleteDownload(trackId: String) {
        viewModelScope.launch {
            libraryRepository.deleteDownload(trackId)
        }
    }

    fun deleteArtist(artistName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            libraryRepository.deleteArtist(artistName)
        }
    }

    fun renameArtist(oldName: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            libraryRepository.renameArtist(oldName, newName)
            if (_selectedArtist.value.equals(oldName, ignoreCase = true)) {
                _selectedArtist.value = newName.trim()
            }
        }
    }

    fun mergeArtists(sourceArtist: String, targetArtist: String) {
        viewModelScope.launch(Dispatchers.IO) {
            libraryRepository.mergeArtists(sourceArtist, targetArtist)
            if (_selectedArtist.value.equals(sourceArtist, ignoreCase = true)) {
                _selectedArtist.value = targetArtist.trim()
            }
        }
    }

    fun updateArtistCover(context: Context, artistName: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val coversDir = java.io.File(context.filesDir, "artist_covers")
                if (!coversDir.exists()) coversDir.mkdirs()
                val safeName = artistName.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val destFile = java.io.File(coversDir, "$safeName.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val fileUri = Uri.fromFile(destFile).toString()
                libraryRepository.updateArtistThumbnail(artistName, fileUri)
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error actualizando portada de artista: ${e.message}", e)
            }
        }
    }

    fun updatePlaylistCover(context: Context, playlistId: Long, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val coversDir = java.io.File(context.filesDir, "playlist_covers")
                if (!coversDir.exists()) coversDir.mkdirs()
                val destFile = java.io.File(coversDir, "$playlistId.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val fileUri = Uri.fromFile(destFile).toString()
                libraryRepository.updatePlaylistThumbnail(playlistId, fileUri)
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error actualizando portada de playlist: ${e.message}", e)
            }
        }
    }

    fun batchSmartParse(tracks: List<TrackEntity>, onComplete: (updatedCount: Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var updated = 0
            for (track in tracks) {
                val (cleanTitle, cleanArtist) = TrackMetadataParser.parse(track.title, track.artistName)
                if (cleanTitle != track.title || cleanArtist != track.artistName) {
                    libraryRepository.updateTrackMetadata(track.id, cleanTitle, cleanArtist)
                    updated++
                }
            }
            withContext(Dispatchers.Main) {
                onComplete(updated)
            }
        }
    }

    fun updateTrackMetadata(trackId: String, title: String, artistName: String) {
        viewModelScope.launch {
            libraryRepository.updateTrackMetadata(trackId, title, artistName)
        }
    }

    fun exportFullLibraryToUri(
        context: Context,
        uri: Uri,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTracks = trackDao.getSavedTracksSync()
                val allArtists = artistDao.getAllArtistsSync()
                val allPlaylists = playlistDao.getAllPlaylistsSync()

                val fullBackup = FullLibraryBackupDto(
                    version = 1,
                    timestamp = System.currentTimeMillis(),
                    tracks = allTracks.map {
                        TrackExportDto(
                            videoId = it.id,
                            title = it.title,
                            artist = it.artistName,
                            thumbnailUrl = it.thumbnailUrl,
                            durationMs = it.durationMs,
                            albumName = it.albumName
                        )
                    },
                    artists = allArtists.map {
                        ArtistBackupDto(
                            name = it.name,
                            thumbnailUrl = it.thumbnailUrl,
                            artistThumbnailUrl = it.thumbnailUrl
                        )
                    },
                    playlists = allPlaylists.map { p ->
                        val pSongs = playlistDao.getSongsForPlaylistSync(p.id)
                        PlaylistExportDto(
                            name = p.name,
                            thumbnailUrl = p.thumbnailUrl,
                            playlistThumbnailUrl = p.thumbnailUrl,
                            tracks = pSongs.map { s ->
                                TrackExportDto(
                                    videoId = s.id,
                                    title = s.title,
                                    artist = s.artistName,
                                    thumbnailUrl = s.thumbnailUrl,
                                    durationMs = s.durationMs,
                                    albumName = s.albumName
                                )
                            }
                        )
                    }
                )

                val jsonString = Json { prettyPrint = true; ignoreUnknownKeys = true }.encodeToString(
                    FullLibraryBackupDto.serializer(),
                    fullBackup
                )
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                withContext(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error al exportar biblioteca completa: ${e.message}", e)
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun importFullLibraryFromUri(
        context: Context,
        uri: Uri,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } ?: throw IllegalStateException("No se pudo abrir el archivo seleccionado.")

                val cleanedJson = jsonString.trim().removePrefix("\uFEFF").trim()
                if (cleanedJson.isEmpty()) {
                    throw IllegalStateException("El archivo seleccionado está vacío.")
                }

                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }

                val jsonElement = json.parseToJsonElement(cleanedJson)
                var importedTracksCount = 0
                var importedPlaylistsCount = 0

                // Caso 1: FullLibraryBackupDto (tiene "tracks" o "artists")
                if (jsonElement is JsonObject && ("tracks" in jsonElement || "artists" in jsonElement)) {
                    val fullBackup = json.decodeFromJsonElement(FullLibraryBackupDto.serializer(), jsonElement)

                    for (trackDto in fullBackup.tracks) {
                        val videoId = trackDto.videoId.trim()
                        if (videoId.isEmpty()) continue

                        val existing = trackDao.getTrackSync(videoId)
                        val thumbUrl = trackDto.thumbnailUrl?.takeIf { it.isNotBlank() }
                            ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                        if (existing == null) {
                            val newEntity = TrackEntity(
                                id = videoId,
                                title = trackDto.title.ifBlank { "Sin título" },
                                artistName = trackDto.artist.ifBlank { "Desconocido" },
                                albumName = trackDto.albumName ?: "Sencillos",
                                durationMs = trackDto.durationMs,
                                thumbnailUrl = thumbUrl,
                                isSaved = true,
                                isDownloaded = false,
                                timestamp = System.currentTimeMillis()
                            )
                            trackDao.insertTrack(newEntity)
                            importedTracksCount++
                        } else {
                            val updatedTitle = if (trackDto.title.isNotBlank() && !trackDto.title.startsWith("Pista ") && trackDto.title != "Desconocido") trackDto.title else existing.title
                            val updatedArtist = if (trackDto.artist.isNotBlank() && trackDto.artist != "Desconocido" && trackDto.artist != "Artista Desconocido") trackDto.artist else existing.artistName
                            val updatedDuration = if (trackDto.durationMs > 0) trackDto.durationMs else existing.durationMs
                            val updatedThumb = if (!thumbUrl.isNullOrBlank()) thumbUrl else existing.thumbnailUrl
                            trackDao.updateTrack(
                                existing.copy(
                                    title = updatedTitle,
                                    artistName = updatedArtist,
                                    durationMs = updatedDuration,
                                    thumbnailUrl = updatedThumb,
                                    isSaved = true
                                )
                            )
                        }
                    }

                    for (artistDto in fullBackup.artists) {
                        val artistName = artistDto.name.trim()
                        if (artistName.isEmpty()) continue
                        val thumb = artistDto.artistThumbnailUrl?.takeIf { it.isNotBlank() }
                            ?: artistDto.thumbnailUrl?.takeIf { it.isNotBlank() }
                        val existingArtist = artistDao.getArtistSync(artistName)
                        if (existingArtist == null) {
                            artistDao.insertArtist(
                                ArtistEntity(
                                    name = artistName,
                                    thumbnailUrl = thumb
                                )
                            )
                        } else if (existingArtist.thumbnailUrl.isNullOrBlank() && !thumb.isNullOrBlank()) {
                            artistDao.insertArtist(
                                existingArtist.copy(thumbnailUrl = thumb)
                            )
                        }
                    }

                    val existingPlaylists = playlistDao.getAllPlaylistsSync()
                    for (p in fullBackup.playlists) {
                        val playlistName = p.name.trim().ifEmpty { "Playlist Importada" }
                        val thumb = p.playlistThumbnailUrl?.takeIf { it.isNotBlank() }
                            ?: p.thumbnailUrl?.takeIf { it.isNotBlank() }
                        val existingPlaylist = existingPlaylists.firstOrNull { it.name.equals(playlistName, ignoreCase = true) }
                        val playlistId = existingPlaylist?.id ?: playlistDao.insertPlaylist(
                            PlaylistEntity(
                                name = playlistName,
                                thumbnailUrl = thumb
                            )
                        )
                        if (existingPlaylist != null && existingPlaylist.thumbnailUrl.isNullOrBlank() && !thumb.isNullOrBlank()) {
                            playlistDao.updatePlaylistThumbnail(existingPlaylist.id, thumb)
                        }
                        if (existingPlaylist == null) importedPlaylistsCount++

                        for (trackDto in p.tracks) {
                            val videoId = trackDto.videoId.trim()
                            if (videoId.isEmpty()) continue

                            val thumbUrl = trackDto.thumbnailUrl?.takeIf { it.isNotBlank() }
                                ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                            val existingTrack = trackDao.getTrackSync(videoId)
                            if (existingTrack == null) {
                                trackDao.insertTrack(
                                    TrackEntity(
                                        id = videoId,
                                        title = trackDto.title.ifBlank { "Sin título" },
                                        artistName = trackDto.artist.ifBlank { "Desconocido" },
                                        albumName = trackDto.albumName ?: "Sencillos",
                                        durationMs = trackDto.durationMs,
                                        thumbnailUrl = thumbUrl,
                                        isSaved = false,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                val updatedTitle = if (trackDto.title.isNotBlank() && !trackDto.title.startsWith("Pista ") && trackDto.title != "Desconocido") trackDto.title else existingTrack.title
                                val updatedArtist = if (trackDto.artist.isNotBlank() && trackDto.artist != "Desconocido" && trackDto.artist != "Artista Desconocido") trackDto.artist else existingTrack.artistName
                                val updatedDuration = if (trackDto.durationMs > 0) trackDto.durationMs else existingTrack.durationMs
                                val updatedThumb = if (!thumbUrl.isNullOrBlank()) thumbUrl else existingTrack.thumbnailUrl
                                trackDao.updateTrack(
                                    existingTrack.copy(
                                        title = updatedTitle,
                                        artistName = updatedArtist,
                                        durationMs = updatedDuration,
                                        thumbnailUrl = updatedThumb
                                    )
                                )
                            }
                            playlistDao.insertSongToPlaylist(
                                PlaylistSongCrossRef(
                                    playlistId = playlistId,
                                    trackId = videoId,
                                    addedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    val msg = "Biblioteca importada: $importedTracksCount canciones y $importedPlaylistsCount playlists nuevas"
                    withContext(Dispatchers.Main) { onResult(true, msg) }
                    return@launch
                }

                // Caso 2: Importación de playlists únicamente (fallback compatible)
                importPlaylistsFromUri(context, uri, onResult)
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Fallo al importar biblioteca: ", e)
                val errorMsg = e.localizedMessage ?: e.message ?: "Error desconocido"
                withContext(Dispatchers.Main) { onResult(false, "Fallo al importar: $errorMsg") }
            }
        }
    }

    fun exportSinglePlaylistToUri(
        context: Context,
        uri: Uri,
        playlistName: String,
        tracks: List<TrackEntity>,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dto = PlaylistExportDto(
                    name = playlistName,
                    thumbnailUrl = tracks.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl,
                    tracks = tracks.map {
                        TrackExportDto(
                            videoId = it.id,
                            title = it.title,
                            artist = it.artistName,
                            thumbnailUrl = it.thumbnailUrl,
                            durationMs = it.durationMs,
                            albumName = it.albumName
                        )
                    }
                )
                val jsonString = Json { prettyPrint = true; ignoreUnknownKeys = true }.encodeToString(PlaylistExportDto.serializer(), dto)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                withContext(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error al exportar playlist: ${e.message}", e)
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun exportAllPlaylistsToUri(
        context: Context,
        uri: Uri,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allPlaylists = playlistDao.getAllPlaylistsSync()
                val backupDto = PlaylistBackupDto(
                    version = 1,
                    playlists = allPlaylists.map { p ->
                        val songs = playlistDao.getSongsForPlaylistSync(p.id)
                        PlaylistExportDto(
                            name = p.name,
                            thumbnailUrl = p.thumbnailUrl,
                            tracks = songs.map { s ->
                                TrackExportDto(
                                    videoId = s.id,
                                    title = s.title,
                                    artist = s.artistName,
                                    thumbnailUrl = s.thumbnailUrl,
                                    durationMs = s.durationMs,
                                    albumName = s.albumName
                                )
                            }
                        )
                    }
                )
                val jsonString = Json { prettyPrint = true; ignoreUnknownKeys = true }.encodeToString(PlaylistBackupDto.serializer(), backupDto)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                }
                withContext(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error al exportar todas las playlists: ${e.message}", e)
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun importPlaylistsFromUri(
        context: Context,
        uri: Uri,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                } ?: throw IllegalStateException("No se pudo abrir el archivo seleccionado.")

                val cleanedJson = jsonString.trim().removePrefix("\uFEFF").trim()
                if (cleanedJson.isEmpty()) {
                    throw IllegalStateException("El archivo seleccionado está vacío.")
                }

                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }

                val jsonElement = json.parseToJsonElement(cleanedJson)
                val playlistsToImport: List<PlaylistExportDto> = when {
                    jsonElement is JsonObject && "playlists" in jsonElement -> {
                        json.decodeFromJsonElement(PlaylistBackupDto.serializer(), jsonElement).playlists
                    }
                    jsonElement is JsonObject -> {
                        listOf(json.decodeFromJsonElement(PlaylistExportDto.serializer(), jsonElement))
                    }
                    jsonElement is JsonArray -> {
                        json.decodeFromJsonElement(
                            kotlinx.serialization.builtins.ListSerializer(PlaylistExportDto.serializer()),
                            jsonElement
                        )
                    }
                    else -> emptyList()
                }

                if (playlistsToImport.isEmpty()) {
                    throw IllegalStateException("No se encontraron playlists válidas en el archivo.")
                }

                for (p in playlistsToImport) {
                    val playlistName = p.name.trim().ifEmpty { "Playlist Importada" }
                    val newPlaylistId = playlistDao.insertPlaylist(
                        PlaylistEntity(
                            name = playlistName,
                            thumbnailUrl = (p.playlistThumbnailUrl ?: p.thumbnailUrl)?.takeIf { it.isNotBlank() }
                        )
                    )

                    for (trackDto in p.tracks) {
                        val videoId = trackDto.videoId.trim()
                        if (videoId.isEmpty()) continue

                        val thumbUrl = trackDto.thumbnailUrl?.takeIf { it.isNotBlank() }
                            ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                        val existing = trackDao.getTrackSync(videoId)
                        if (existing == null) {
                            val isRealAlbum = !trackDto.albumName.isNullOrBlank() &&
                                    !trackDto.albumName.equals(trackDto.artist, ignoreCase = true) &&
                                    !trackDto.albumName.equals("Sencillos", ignoreCase = true) &&
                                    !trackDto.albumName.equals("YouTube", ignoreCase = true)
                            val effectiveAlbum = if (isRealAlbum) trackDto.albumName else "Sencillos"

                            val newEntity = TrackEntity(
                                id = videoId,
                                title = trackDto.title.ifBlank { "Sin título" },
                                artistName = trackDto.artist.ifBlank { "Desconocido" },
                                albumName = effectiveAlbum,
                                durationMs = trackDto.durationMs,
                                thumbnailUrl = thumbUrl,
                                format = null,
                                isDownloaded = false,
                                isSaved = false,
                                localPath = null,
                                timestamp = System.currentTimeMillis()
                            )
                            trackDao.insertTrack(newEntity)
                        } else {
                            val updatedTitle = if (trackDto.title.isNotBlank() && !trackDto.title.startsWith("Pista ") && trackDto.title != "Desconocido") trackDto.title else existing.title
                            val updatedArtist = if (trackDto.artist.isNotBlank() && trackDto.artist != "Desconocido" && trackDto.artist != "Artista Desconocido") trackDto.artist else existing.artistName
                            val updatedDuration = if (trackDto.durationMs > 0) trackDto.durationMs else existing.durationMs
                            val updatedThumb = if (!thumbUrl.isNullOrBlank()) thumbUrl else existing.thumbnailUrl
                            trackDao.updateTrack(
                                existing.copy(
                                    title = updatedTitle,
                                    artistName = updatedArtist,
                                    durationMs = updatedDuration,
                                    thumbnailUrl = updatedThumb
                                )
                            )
                        }

                        playlistDao.insertSongToPlaylist(
                            PlaylistSongCrossRef(
                                playlistId = newPlaylistId,
                                trackId = videoId,
                                addedAt = System.currentTimeMillis()
                            )
                        )
                    }

                    val thumb = (p.playlistThumbnailUrl ?: p.thumbnailUrl)?.takeIf { it.isNotBlank() }
                    if (!thumb.isNullOrBlank()) {
                        playlistDao.updatePlaylistThumbnail(newPlaylistId, thumb)
                    } else {
                        val firstThumb = p.tracks.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
                            ?: p.tracks.firstOrNull()?.let { "https://i.ytimg.com/vi/${it.videoId}/hqdefault.jpg" }
                        if (!firstThumb.isNullOrBlank()) {
                            playlistDao.updatePlaylistThumbnail(newPlaylistId, firstThumb)
                        }
                    }
                }

                val msg = if (playlistsToImport.size == 1) {
                    "Playlist '${playlistsToImport.first().name}' importada con éxito (${playlistsToImport.first().tracks.size} canciones)"
                } else {
                    "${playlistsToImport.size} Playlists importadas con éxito"
                }
                withContext(Dispatchers.Main) { onResult(true, msg) }
            } catch (e: Exception) {
                Log.e("ImportError", "Fallo al importar: ", e)
                val errorMsg = e.localizedMessage ?: e.message ?: "Error desconocido"
                withContext(Dispatchers.Main) { onResult(false, "Fallo al importar: $errorMsg") }
            }
        }
    }

    fun downloadAll(tracks: List<Track>) {
        viewModelScope.launch {
            tracks.forEach { track ->
                downloadRepository.download(track)
            }
        }
    }
}
