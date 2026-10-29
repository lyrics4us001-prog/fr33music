package com.fr33music.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.domain.model.ExtractorError
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSource: SearchSource,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

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

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            val result = searchSource.search(currentQuery)

            result.fold(
                onSuccess = { tracks ->
                    if (tracks.isEmpty()) {
                        _uiState.value = SearchUiState.Empty
                    } else {
                        _uiState.value = SearchUiState.Success(tracks)
                    }
                },
                onFailure = { throwable ->
                    val message = when (throwable) {
                        is ExtractorError.EmptyQuery -> "Introduce un término de búsqueda"
                        is ExtractorError.NetworkError -> "Error de conexión. Comprueba tu conexión a Internet."
                        is ExtractorError.StreamUnavailable -> "Contenido no disponible en YouTube."
                        else -> throwable.message ?: "Ocurrió un error al buscar. Inténtalo de nuevo."
                    }
                    _uiState.value = SearchUiState.Error(message)
                }
            )
        }
    }

    fun retry() {
        search()
    }

    fun onTrackClick(track: Track) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SearchUiState.Success) {
                val index = currentState.tracks.indexOfFirst { it.trackId == track.trackId }
                if (index >= 0) {
                    playerRepository.setQueue(currentState.tracks, index)
                    return@launch
                }
            }
            playerRepository.play(track)
        }
    }
}
