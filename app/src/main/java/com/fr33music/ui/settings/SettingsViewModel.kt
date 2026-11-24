package com.fr33music.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import com.fr33music.core.database.dao.SearchHistoryDao
import com.fr33music.core.database.datastore.SettingsDataStore
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.extractor.ExtractorInfo
import com.fr33music.core.updater.ApkInstaller
import com.fr33music.core.updater.UpdateChecker
import com.fr33music.core.updater.UpdateDownloader
import com.fr33music.core.updater.model.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val appVersion: String = "1.0.0",
    val extractorVersion: String = ExtractorInfo.VERSION,
    val isCheckingUpdate: Boolean = false,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Float = 0f,
    val availableUpdate: UpdateInfo? = null,
    val showNoUpdateToast: Boolean = false,
    val updateError: String? = null,
    val feedbackMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
    private val searchHistoryDao: SearchHistoryDao,
    private val downloadRepository: DownloadRepository,
    private val updateChecker: UpdateChecker,
    private val updateDownloader: UpdateDownloader,
    private val apkInstaller: ApkInstaller
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(appVersion = getAppVersionName())
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
                            updateError = "Error al verificar actualizaciones: ${error.message}"
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

    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryDao.clearHistory()
            _uiState.update { it.copy(feedbackMessage = "Historial de búsqueda eliminado") }
        }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun clearAppCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageLoader = Coil.imageLoader(context)
                imageLoader.diskCache?.clear()
                imageLoader.memoryCache?.clear()
                context.cacheDir.deleteRecursively()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(feedbackMessage = "Caché de red e imágenes limpiada") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(feedbackMessage = "Error al limpiar caché: ${e.message}") }
                }
            }
        }
    }

    fun clearAllDownloadedAudios() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                downloadRepository.clearAllDownloadedAudios()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(feedbackMessage = "Archivos de audio descargados eliminados") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(feedbackMessage = "Error al borrar audios: ${e.message}") }
                }
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    private fun getAppVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }
}
