package com.fr33music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.ArtistDao
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.entity.ArtistEntity
import com.fr33music.core.database.entity.TrackEntity
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.Track
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab(val title: String) {
    ARTISTS("Artistas"),
    PLAYLISTS("Playlists"),
    ALL_SONGS("Todas las canciones")
}

data class ArtistItem(
    val name: String,
    val trackCount: Int,
    val thumbnailUrl: String?,
    val firstTrackId: String? = null
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
    val filteredTracks: List<TrackEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val downloadRepository: DownloadRepository,
    private val playerRepository: PlayerRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.ARTISTS)
    private val _selectedArtist = MutableStateFlow<String?>(null)
    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    private val _selectedPlaylistName = MutableStateFlow<String?>(null)

    private data class FilterSelection(
        val tab: LibraryTab = LibraryTab.ARTISTS,
        val artist: String? = null,
        val playlistId: Long? = null,
        val playlistName: String? = null
    )

    private val filterSelection = combine(
        _selectedTab,
        _selectedArtist,
        _selectedPlaylistId,
        _selectedPlaylistName
    ) { tab, artist, playlistId, playlistName ->
        FilterSelection(tab, artist, playlistId, playlistName)
    }

    private data class LibraryTracks(
        val saved: List<TrackEntity>,
        val downloaded: List<TrackEntity>
    )

    private val tracksFlow = combine(
        trackDao.getSavedTracks(),
        trackDao.getDownloadedTracks()
    ) { saved, downloaded ->
        LibraryTracks(saved, downloaded)
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
        tracksFlow,
        artistDao.getAllArtists(),
        libraryRepository.getPlaylists(),
        playlistSongsFlow
    ) { filter, tracks, artistEntities, playlistsList, playlistSongs ->
        val saved = tracks.saved
        val downloads = tracks.downloaded
        val allLibraryTracks = (saved + downloads).distinctBy { it.id }

        // Lista de "Todas las canciones": ordenadas alfabéticamente por título
        val allSongsSorted = allLibraryTracks.sortedBy { it.title.lowercase() }

        // Agrupación por artista con fallback de miniatura y trackId
        val artistItems = allLibraryTracks
            .groupBy { it.artistName }
            .map { (artistName, trackList) ->
                val entity = artistEntities.firstOrNull { it.name.equals(artistName, ignoreCase = true) }
                val thumb = entity?.thumbnailUrl
                    ?: trackList.firstOrNull { !it.thumbnailUrl.isNullOrBlank() }?.thumbnailUrl
                ArtistItem(
                    name = artistName,
                    trackCount = trackList.size,
                    thumbnailUrl = thumb,
                    firstTrackId = null
                )
            }
            .sortedBy { it.name.lowercase() }

        val filtered = when {
            filter.artist != null -> allLibraryTracks.filter { it.artistName.equals(filter.artist, ignoreCase = true) }
            else -> emptyList()
        }

        LibraryUiState(
            selectedTab = filter.tab,
            artists = artistItems,
            playlists = playlistsList,
            allSongs = allSongsSorted,
            selectedArtist = filter.artist,
            selectedPlaylistId = filter.playlistId,
            selectedPlaylistName = filter.playlistName,
            playlistSongs = playlistSongs,
            filteredTracks = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

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

    fun downloadAll(tracks: List<Track>) {
        viewModelScope.launch {
            tracks.forEach { track ->
                downloadRepository.download(track)
            }
        }
    }
}
