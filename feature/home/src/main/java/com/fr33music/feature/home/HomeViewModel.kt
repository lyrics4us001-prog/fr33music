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
import com.fr33music.core.updater.ApkInstaller
import com.fr33music.core.updater.UpdateChecker
import com.fr33music.core.updater.UpdateDownloader
import com.fr33music.core.updater.model.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val recommendationTitle: String = "Descubre nueva música",
    val recommendations: List<Track> = emptyList(),
    val showSettings: Boolean = false,
    val extractorVersion: String = ExtractorInfo.VERSION,
    val appVersion: String = "1.0.0",
    val cacheClearedMessage: String? = null,
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Float = 0f,
    val availableUpdate: UpdateInfo? = null,
    val showNoUpdateToast: Boolean = false,
    val updateError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchHistoryDao: SearchHistoryDao,
    private val searchSource: SearchSource,
    private val playerRepository: PlayerRepository,
    private val updateChecker: UpdateChecker,
    private val updateDownloader: UpdateDownloader,
    private val apkInstaller: ApkInstaller
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(appVersion = getAppVersionName())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRecommendations()
    }

    private fun getAppVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recommendationTitle = title,
                            recommendations = tracks
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recommendationTitle = title,
                            recommendations = emptyList()
                        )
                    }
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
        _uiState.update { it.copy(showSettings = true, cacheClearedMessage = null, updateError = null) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettings = false) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearHistory()
            _uiState.update { it.copy(cacheClearedMessage = "Historial de búsqueda eliminado") }
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
                    _uiState.update { it.copy(cacheClearedMessage = "Caché de red e imágenes limpiada") }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(cacheClearedMessage = "Error al limpiar caché") }
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true, updateError = null, showNoUpdateToast = false) }
            val currentVersion = _uiState.value.appVersion
            val result = updateChecker.checkForUpdate(currentVersion)
            result.fold(
                onSuccess = { updateInfo ->
                    if (updateInfo != null) {
                        _uiState.update {
                            it.copy(
                                isCheckingUpdate = false,
                                availableUpdate = updateInfo
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isCheckingUpdate = false,
                                showNoUpdateToast = true
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCheckingUpdate = false,
                            updateError = error.message ?: "Error al verificar actualizaciones"
                        )
                    }
                }
            )
        }
    }

    fun downloadAndInstallUpdate() {
        val update = _uiState.value.availableUpdate ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingUpdate = true, downloadProgress = 0f, updateError = null) }
            val result = updateDownloader.downloadApk(update.downloadUrl) { progress ->
                _uiState.update { it.copy(downloadProgress = progress) }
            }
            result.fold(
                onSuccess = { apkFile ->
                    _uiState.update { it.copy(isDownloadingUpdate = false, availableUpdate = null) }
                    apkInstaller.installApk(apkFile)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDownloadingUpdate = false,
                            updateError = "Error al descargar: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(availableUpdate = null) }
    }

    fun onNoUpdateToastShown() {
        _uiState.update { it.copy(showNoUpdateToast = false) }
    }

    fun clearUpdateError() {
        _uiState.update { it.copy(updateError = null) }
    }
}
