package com.fr33music.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.domain.model.Track
import com.fr33music.core.extractor.ExtractorInfo
import com.fr33music.core.extractor.SearchSource
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val recommendationTitle: String = "Descubre nueva música",
    val recommendations: List<Track> = emptyList(),
    val showSettings: Boolean = false,
    val extractorVersion: String = ExtractorInfo.VERSION,
    val cacheClearedMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchHistoryDao: SearchHistoryDao,
    private val searchSource: SearchSource,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Obtener búsquedas recientes
            val recentSearches = searchHistoryDao.getRecentSearches(5).first()
            val query = if (recentSearches.isNotEmpty()) {
                recentSearches.first().query
            } else {
                "Tendencias musicales"
            }

            val title = if (recentSearches.isNotEmpty()) {
                "Recomendado por \"$query\""
            } else {
                "Tendencias destacadas"
            }

            val result = searchSource.search(query)
            result.fold(
                onSuccess = { tracks ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendationTitle = title,
                        recommendations = tracks
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recommendationTitle = title,
                        recommendations = emptyList()
                    )
                }
            )
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            val list = _uiState.value.recommendations
            val index = list.indexOfFirst { it.trackId == track.trackId }
            if (index >= 0) {
                playerRepository.setQueue(list, index)
            } else {
                playerRepository.play(track)
            }
        }
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true, cacheClearedMessage = null)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false)
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearHistory()
            _uiState.value = _uiState.value.copy(cacheClearedMessage = "Historial de búsqueda eliminado")
            loadRecommendations()
        }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Limpiar caché de imágenes Coil
                val imageLoader = Coil.imageLoader(context)
                imageLoader.diskCache?.clear()
                imageLoader.memoryCache?.clear()

                // Limpiar caché de archivos temporales
                context.cacheDir.deleteRecursively()

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(cacheClearedMessage = "Caché de red e imágenes limpiada")
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(cacheClearedMessage = "Error al limpiar caché")
                }
            }
        }
    }
}
