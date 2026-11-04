package com.fr33music.feature.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.entity.SearchHistoryEntity
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.AlbumCard
import com.fr33music.core.domain.model.ChannelCard
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSource: SearchSource,
    private val playerRepository: PlayerRepository,
    private val trackDao: TrackDao,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    var albumScrollIndex: Int = 0
    var albumScrollOffset: Int = 0

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<AlbumCard?>(null)
    val selectedAlbum: StateFlow<AlbumCard?> = _selectedAlbum.asStateFlow()

    private val _albumTracks = MutableStateFlow<List<Track>>(emptyList())
    val albumTracks: StateFlow<List<Track>> = _albumTracks.asStateFlow()

    private val _isLoadingAlbum = MutableStateFlow(false)
    val isLoadingAlbum: StateFlow<Boolean> = _isLoadingAlbum.asStateFlow()

    private val _selectedChannel = MutableStateFlow<ChannelCard?>(null)
    val selectedChannel: StateFlow<ChannelCard?> = _selectedChannel.asStateFlow()

    private val _channelVideos = MutableStateFlow<List<Track>>(emptyList())
    val channelVideos: StateFlow<List<Track>> = _channelVideos.asStateFlow()

    private val _isLoadingChannel = MutableStateFlow(false)
    val isLoadingChannel: StateFlow<Boolean> = _isLoadingChannel.asStateFlow()

    fun selectChannel(channel: ChannelCard?) {
        _selectedChannel.value = channel
        if (channel != null) {
            viewModelScope.launch {
                try {
                    _isLoadingChannel.value = true
                    val result = searchSource.getChannelVideos(channel)
                    _channelVideos.value = result.getOrElse { emptyList() }
                } catch (e: Exception) {
                    _channelVideos.value = emptyList()
                } finally {
                    _isLoadingChannel.value = false
                }
            }
        } else {
            _channelVideos.value = emptyList()
            _isLoadingChannel.value = false
        }
    }

    fun selectAlbum(album: AlbumCard?) {
        _selectedAlbum.value = album
        if (album != null) {
            viewModelScope.launch {
                try {
                    _isLoadingAlbum.value = true
                    val result = searchSource.getAlbumTracks(album)
                    _albumTracks.value = result.getOrElse { emptyList() }
                } catch (e: Exception) {
                    _albumTracks.value = emptyList()
                } finally {
                    _isLoadingAlbum.value = false
                }
            }
        } else {
            _albumTracks.value = emptyList()
        }
    }

    val savedTrackIds: StateFlow<Set<String>> = trackDao.getSavedTracks()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val playlists: StateFlow<List<PlaylistWithSongCount>> = libraryRepository.getPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addSongToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            try {
                libraryRepository.addSongToPlaylist(playlistId, track)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al añadir canción a playlist: ${e.message}", e)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                libraryRepository.createPlaylist(name)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al crear playlist: ${e.message}", e)
            }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            try {
                libraryRepository.deletePlaylist(id)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al eliminar playlist: ${e.message}", e)
            }
        }
    }

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.trim().isEmpty()) {
            searchJob?.cancel()
            _uiState.value = SearchUiState.Idle
        }
    }

    fun search() {
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        // Registrar búsqueda en historial para recomendaciones
        viewModelScope.launch {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = currentQuery))
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            try {
                val result = searchSource.searchLookingDetails(currentQuery)
                result.fold(
                    onSuccess = { lookingRes ->
                        val sortedChannels = lookingRes.channels.sortedByDescending { it.subscriberCount }
                        val sortedResult = lookingRes.copy(channels = sortedChannels)
                        if (sortedResult.channels.isEmpty() && sortedResult.videos.isEmpty() && sortedResult.playlists.isEmpty()) {
                            _uiState.value = SearchUiState.Empty
                        } else {
                            _uiState.value = SearchUiState.LookingSuccess(sortedResult)
                        }
                    },
                    onFailure = { throwable ->
                        handleError(throwable)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        val message = when (throwable) {
            is ExtractorError.EmptyQuery -> "Introduce un término de búsqueda"
            is ExtractorError.NetworkError -> "Error de conexión. Comprueba tu conexión a Internet."
            is ExtractorError.StreamUnavailable -> "Contenido no disponible en YouTube."
            else -> throwable.message ?: "Ocurrió un error al buscar. Inténtalo de nuevo."
        }
        _uiState.value = SearchUiState.Error(message)
    }

    fun retry() {
        search()
    }

    fun onTrackClick(track: Track) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                when (currentState) {
                    is SearchUiState.LookingSuccess -> {
                        val index = currentState.result.videos.indexOfFirst { it.trackId == track.trackId }
                        if (index >= 0) {
                            playerRepository.setQueue(currentState.result.videos, index)
                            return@launch
                        }
                    }
                    is SearchUiState.Success -> {
                        val index = currentState.tracks.indexOfFirst { it.trackId == track.trackId }
                        if (index >= 0) {
                            playerRepository.setQueue(currentState.tracks, index)
                            return@launch
                        }
                    }
                    else -> {}
                }
                playerRepository.play(track)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al reproducir canción ${track.trackId}: ${e.message}", e)
            }
        }
    }

    fun playAll(tracks: List<Track>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        viewModelScope.launch {
            try {
                if (shuffle) {
                    val shuffled = tracks.shuffled()
                    playerRepository.setQueue(shuffled, 0)
                    if (!playerRepository.playbackState.value.isShuffle) {
                        playerRepository.toggleShuffle()
                    }
                } else {
                    val index = startIndex.coerceIn(0, tracks.lastIndex)
                    playerRepository.setQueue(tracks, index)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en playAll: ${e.message}", e)
            }
        }
    }

    fun toggleSave(track: Track) {
        viewModelScope.launch {
            try {
                val isCurrentlySaved = savedTrackIds.value.contains(track.trackId)
                libraryRepository.toggleSave(track, isCurrentlySaved)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en toggleSave: ${e.message}", e)
            }
        }
    }

    fun downloadAll(tracks: List<Track>) {
        viewModelScope.launch {
            try {
                tracks.forEach { track ->
                    downloadRepository.download(track)
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en downloadAll: ${e.message}", e)
            }
        }
    }
}
