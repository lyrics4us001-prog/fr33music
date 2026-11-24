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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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

    private val _isLoadingMoreChannel = MutableStateFlow(false)
    val isLoadingMoreChannel: StateFlow<Boolean> = _isLoadingMoreChannel.asStateFlow()

    private val _isSearchingInChannel = MutableStateFlow(false)
    val isSearchingInChannel: StateFlow<Boolean> = _isSearchingInChannel.asStateFlow()

    private val _channelSearchQuery = MutableStateFlow("")
    val channelSearchQuery: StateFlow<String> = _channelSearchQuery.asStateFlow()

    private val _channelHasMore = MutableStateFlow(false)
    val channelHasMore: StateFlow<Boolean> = _channelHasMore.asStateFlow()

    private val _channelError = MutableStateFlow<String?>(null)
    val channelError: StateFlow<String?> = _channelError.asStateFlow()

    private val _channelTotalVideos = MutableStateFlow<Long?>(null)
    val channelTotalVideos: StateFlow<Long?> = _channelTotalVideos.asStateFlow()

    private val _channelUploads = MutableStateFlow<List<Track>>(emptyList())
    private var channelUploadsNextPageToken: String? = null
    private val _channelSearchResults = MutableStateFlow<List<Track>>(emptyList())
    private var channelSearchNextPageToken: String? = null
    private var channelSearchJob: Job? = null

    fun selectChannel(channel: ChannelCard?) {
        _selectedChannel.value = channel
        channelSearchJob?.cancel()
        _channelSearchQuery.value = ""
        _channelError.value = null
        _channelTotalVideos.value = null
        if (channel != null) {
            _channelUploads.value = emptyList()
            channelUploadsNextPageToken = null
            _channelSearchResults.value = emptyList()
            channelSearchNextPageToken = null
            _channelVideos.value = emptyList()
            _isLoadingChannel.value = true
            _isLoadingMoreChannel.value = false
            _isSearchingInChannel.value = false
            _channelHasMore.value = false

            viewModelScope.launch {
                try {
                    val result = searchSource.getChannelVideosPaged(channel, null)
                    if (result.isSuccess) {
                        val res = result.getOrThrow()
                        _channelUploads.value = res.tracks
                        channelUploadsNextPageToken = res.nextPageToken
                        _channelTotalVideos.value = res.totalVideos ?: res.tracks.size.toLong()
                        _channelVideos.value = res.tracks
                        _channelHasMore.value = res.nextPageToken != null
                        _channelError.value = null
                    } else {
                        _channelUploads.value = emptyList()
                        _channelVideos.value = emptyList()
                        _channelTotalVideos.value = null
                        _channelHasMore.value = false
                        _channelError.value = "Error al cargar los vídeos del canal"
                    }
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "Error al cargar canal ${channel.name}: ${e.message}", e)
                    _channelVideos.value = emptyList()
                    _channelTotalVideos.value = null
                    _channelError.value = "Error al cargar los vídeos del canal"
                } finally {
                    _isLoadingChannel.value = false
                }
            }
        } else {
            _channelVideos.value = emptyList()
            _channelUploads.value = emptyList()
            channelUploadsNextPageToken = null
            _channelSearchResults.value = emptyList()
            channelSearchNextPageToken = null
            _channelTotalVideos.value = null
            _isLoadingChannel.value = false
            _isLoadingMoreChannel.value = false
            _isSearchingInChannel.value = false
            _channelHasMore.value = false
        }
    }

    fun loadMoreChannelVideos() {
        if (_isLoadingChannel.value || _isLoadingMoreChannel.value || _isSearchingInChannel.value) return
        val currentChannel = _selectedChannel.value ?: return

        // 1. Modo búsqueda interna
        if (_channelSearchQuery.value.isNotBlank()) {
            val token = channelSearchNextPageToken ?: return
            _isLoadingMoreChannel.value = true
            _channelError.value = null
            viewModelScope.launch {
                try {
                    val result = searchSource.searchChannelVideos(currentChannel, _channelSearchQuery.value, token)
                    if (result.isSuccess) {
                        val res = result.getOrThrow()
                        val existingIds = _channelSearchResults.value.map { it.trackId }.toSet()
                        val newTracks = res.tracks.filter { it.trackId !in existingIds }
                        _channelSearchResults.value = _channelSearchResults.value + newTracks
                        channelSearchNextPageToken = res.nextPageToken
                        _channelVideos.value = _channelSearchResults.value
                        _channelHasMore.value = res.nextPageToken != null
                    } else {
                        _channelError.value = "Error al cargar más resultados"
                    }
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "Error en paginación de búsqueda de canal: ${e.message}", e)
                    _channelError.value = "Error al cargar más resultados"
                } finally {
                    _isLoadingMoreChannel.value = false
                }
            }
            return
        }

        // 2. Modo vídeos subidos (uploadsPlaylist)
        val token = channelUploadsNextPageToken ?: return
        _isLoadingMoreChannel.value = true
        _channelError.value = null
        viewModelScope.launch {
            try {
                val result = searchSource.getChannelVideosPaged(currentChannel, token)
                if (result.isSuccess) {
                    val res = result.getOrThrow()
                    val existingIds = _channelUploads.value.map { it.trackId }.toSet()
                    val newTracks = res.tracks.filter { it.trackId !in existingIds }
                    _channelUploads.value = _channelUploads.value + newTracks
                    channelUploadsNextPageToken = res.nextPageToken
                    _channelVideos.value = _channelUploads.value
                    _channelHasMore.value = res.nextPageToken != null
                } else {
                    _channelError.value = "Error al cargar más vídeos"
                }
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en paginación de subidas de canal: ${e.message}", e)
                _channelError.value = "Error al cargar más vídeos"
            } finally {
                _isLoadingMoreChannel.value = false
            }
        }
    }

    fun retryLoadMoreChannelVideos() {
        _channelError.value = null
        loadMoreChannelVideos()
    }

    fun searchInChannel(query: String) {
        _channelSearchQuery.value = query
        _channelError.value = null
        channelSearchJob?.cancel()

        if (query.isBlank()) {
            // Restaurar instantáneamente la lista de subidas que ya se tenía cargada
            _channelSearchResults.value = emptyList()
            channelSearchNextPageToken = null
            _isSearchingInChannel.value = false
            _channelVideos.value = _channelUploads.value
            _channelHasMore.value = channelUploadsNextPageToken != null
            return
        }

        val currentChannel = _selectedChannel.value ?: return
        channelSearchJob = viewModelScope.launch {
            delay(350)
            if (!isActive) return@launch

            _isSearchingInChannel.value = true
            _channelVideos.value = emptyList() // Limpiar lista al iniciar búsqueda
            try {
                val result = searchSource.searchChannelVideos(currentChannel, query, null)
                if (isActive) {
                    if (result.isSuccess) {
                        val res = result.getOrThrow()
                        _channelSearchResults.value = res.tracks
                        channelSearchNextPageToken = res.nextPageToken
                        _channelVideos.value = res.tracks
                        _channelHasMore.value = res.nextPageToken != null
                    } else {
                        _channelSearchResults.value = emptyList()
                        channelSearchNextPageToken = null
                        _channelVideos.value = emptyList()
                        _channelHasMore.value = false
                        _channelError.value = "No se encontraron vídeos que coincidan"
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e("SearchViewModel", "Error al buscar en canal ${currentChannel.name}: ${e.message}", e)
                    _channelSearchResults.value = emptyList()
                    channelSearchNextPageToken = null
                    _channelVideos.value = emptyList()
                    _channelHasMore.value = false
                    _channelError.value = "Error al realizar la búsqueda"
                }
            } finally {
                if (isActive) {
                    _isSearchingInChannel.value = false
                }
            }
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

    val recentSearches: StateFlow<List<String>> = searchHistoryDao.getRecentSearches(3)
        .map { list -> list.map { it.query } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()
    private var suggestionsJob: Job? = null

    fun addSongToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryRepository.addSongToPlaylist(playlistId, track)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al añadir canción a playlist: ${e.message}", e)
            }
        }
    }

    fun addSongsToPlaylist(playlistId: Long, tracks: List<Track>) {
        if (tracks.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryRepository.addSongsToPlaylist(playlistId, tracks)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al añadir canciones masivas a playlist: ${e.message}", e)
            }
        }
    }

    fun saveTracks(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                libraryRepository.saveTracks(tracks)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error al guardar canciones masivas en favoritos: ${e.message}", e)
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
        val trimmed = newQuery.trim()
        if (trimmed.isEmpty()) {
            searchJob?.cancel()
            suggestionsJob?.cancel()
            _suggestions.value = emptyList()
            _uiState.value = SearchUiState.Idle
        } else {
            suggestionsJob?.cancel()
            suggestionsJob = viewModelScope.launch {
                kotlinx.coroutines.delay(200)
                val res = searchSource.getSuggestions(trimmed)
                _suggestions.value = res.getOrElse { emptyList() }
            }
        }
    }

    fun search(customQuery: String? = null) {
        if (customQuery != null) {
            _query.value = customQuery
        }
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }

        _suggestions.value = emptyList()

        // Registrar búsqueda en historial para recomendaciones (mantener solo las 3 más recientes)
        viewModelScope.launch {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = currentQuery))
            searchHistoryDao.trimHistory(3)
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
                // 1. Si hay un canal seleccionado con vídeos cargados
                val channelList = _channelVideos.value
                val channelIdx = channelList.indexOfFirst { it.trackId == track.trackId }
                if (channelIdx >= 0) {
                    playerRepository.setQueue(channelList, channelIdx)
                    return@launch
                }

                // 2. Si hay un álbum o playlist seleccionada con pistas cargadas
                val albumList = _albumTracks.value
                val albumIdx = albumList.indexOfFirst { it.trackId == track.trackId }
                if (albumIdx >= 0) {
                    playerRepository.setQueue(albumList, albumIdx)
                    return@launch
                }

                // 3. Resultados de búsqueda principal (vídeos o canciones)
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
                    playerRepository.setQueue(tracks, 0)
                    if (!playerRepository.playbackState.value.isShuffle) {
                        playerRepository.toggleShuffle()
                    }
                } else {
                    val index = startIndex.coerceIn(0, tracks.lastIndex)
                    if (playerRepository.playbackState.value.isShuffle) {
                        playerRepository.toggleShuffle()
                    }
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

    fun deleteSearchHistoryItem(itemQuery: String) {
        viewModelScope.launch {
            try {
                searchHistoryDao.deleteSearch(itemQuery)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en deleteSearchHistoryItem: ${e.message}", e)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            try {
                searchHistoryDao.clearHistory()
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Error en clearSearchHistory: ${e.message}", e)
            }
        }
    }
}
