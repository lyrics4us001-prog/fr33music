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
import com.fr33music.core.domain.model.PlaylistBackupDto
import com.fr33music.core.domain.model.PlaylistExportDto
import com.fr33music.core.domain.model.Track
import com.fr33music.core.domain.model.TrackExportDto
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
    private val libraryRepository: LibraryRepository
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
        trackDao.getSavedTracks(),
        artistDao.getAllArtists(),
        libraryRepository.getPlaylists(),
        playlistSongsFlow
    ) { filter, savedTracks, artistEntities, playlistsList, playlistSongs ->
        val allLibraryTracks = savedTracks

        // Lista de "Todas las canciones": ordenadas según sortOrder
        val allSongsSorted = when (filter.sortOrder) {
            LibrarySortOrder.RECENTLY_ADDED -> allLibraryTracks.sortedByDescending { it.timestamp }
            LibrarySortOrder.ALPHABETICAL -> allLibraryTracks.sortedWith(
                compareBy({ it.artistName.lowercase() }, { it.title.lowercase() })
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

        // Agrupación por artista con fallback de miniatura y ordenamiento según sortOrder
        val rawArtistItems = allLibraryTracks
            .groupBy { it.artistName }
            .map { (artistName, trackList) ->
                val entity = artistEntities.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
                val thumb = entity?.thumbnailUrl
                    ?: trackList.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
                val maxTimestamp = trackList.maxOfOrNull { it.timestamp } ?: 0L
                ArtistItem(
                    name = artistName,
                    trackCount = trackList.size,
                    thumbnailUrl = thumb,
                    firstTrackId = null,
                    latestTimestamp = maxTimestamp
                )
            }

        val artistItems = when (filter.sortOrder) {
            LibrarySortOrder.RECENTLY_ADDED -> rawArtistItems.sortedByDescending { it.latestTimestamp }
            LibrarySortOrder.ALPHABETICAL -> rawArtistItems.sortedBy { it.name.lowercase() }
        }

        val filtered = when {
            filter.artist != null -> {
                val artistTracks = allLibraryTracks.filter { it.artistName.equals(filter.artist, ignoreCase = true) }
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
                            thumbnailUrl = p.thumbnailUrl?.takeIf { it.isNotBlank() }
                        )
                    )

                    for (trackDto in p.tracks) {
                        val videoId = trackDto.videoId.trim()
                        if (videoId.isEmpty()) continue

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
                                thumbnailUrl = trackDto.thumbnailUrl,
                                format = null,
                                isDownloaded = false,
                                isSaved = false,
                                localPath = null,
                                timestamp = System.currentTimeMillis()
                            )
                            trackDao.insertTrack(newEntity)
                        }

                        playlistDao.insertSongToPlaylist(
                            PlaylistSongCrossRef(
                                playlistId = newPlaylistId,
                                trackId = videoId,
                                addedAt = System.currentTimeMillis()
                            )
                        )
                    }

                    val thumb = p.thumbnailUrl
                    if (!thumb.isNullOrBlank()) {
                        playlistDao.updatePlaylistThumbnail(newPlaylistId, thumb)
                    } else {
                        val firstThumb = p.tracks.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
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
