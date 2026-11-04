package com.fr33music.feature.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.download.DownloadState
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.lyrics.LyricsRepository
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.Track
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val trackDao: TrackDao,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val lyricsRepository: LyricsRepository
) : ViewModel() {

    private val showLyricsSheet = MutableStateFlow(false)
    private val lyricsText = MutableStateFlow<String?>(null)
    private val isLoadingLyrics = MutableStateFlow(false)

    // ID de la pista actual desacoplado del progreso para evitar re-subscripciones constantes
    private val currentTrackId = playerRepository.playbackState
        .map { it.currentTrack?.trackId }
        .distinctUntilChanged()

    // Observar si la pista actual está guardada o descargada en Room
    private val currentTrackEntity = currentTrackId
        .flatMapLatest { id ->
            if (id != null) trackDao.getTrack(id) else flowOf(null)
        }

    // Observar estado de descarga reactivo independiente de play/pause
    val downloadState: StateFlow<DownloadState> = currentTrackId
        .flatMapLatest { id ->
            if (id != null) {
                downloadRepository.getDownloadState(id)
            } else {
                flowOf(DownloadState.Idle)
            }
        }
        .onEach { state ->
            Log.d("Descarga", "Estado actual: $state")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DownloadState.Idle
        )

    private data class LyricsUi(
        val showSheet: Boolean = false,
        val text: String? = null,
        val isLoading: Boolean = false
    )

    private val lyricsUi = combine(
        showLyricsSheet,
        lyricsText,
        isLoadingLyrics
    ) { show, text, loading ->
        LyricsUi(show, text, loading)
    }

    val uiState: StateFlow<PlayerUiState> = combine(
        playerRepository.playbackState,
        currentTrackEntity,
        downloadState,
        lyricsUi
    ) { playback, trackEntity, dlState, lyrics ->
        val track = playback.currentTrack
        val isDownloaded = trackEntity?.isDownloaded == true || dlState is DownloadState.Completed
        val downloadStatusStr = when (dlState) {
            DownloadState.Queued -> DownloadStatus.QUEUED
            is DownloadState.Downloading -> DownloadStatus.DOWNLOADING
            is DownloadState.Completed -> DownloadStatus.COMPLETED
            is DownloadState.Error -> DownloadStatus.ERROR
            DownloadState.Idle -> null
        }
        PlayerUiState(
            track = track,
            isPlaying = playback.isPlaying,
            isLoading = playback.isLoading,
            isShuffle = playback.isShuffle,
            isSaved = trackEntity?.isSaved == true,
            isDownloaded = isDownloaded,
            downloadStatus = downloadStatusStr,
            lyrics = lyrics.text,
            isLoadingLyrics = lyrics.isLoading,
            showLyricsSheet = lyrics.showSheet,
            positionMs = playback.positionMs,
            durationMs = if (playback.durationMs > 0L) playback.durationMs else (track?.durationMs ?: 0L),
            error = playback.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun togglePlayPause() {
        playerRepository.togglePlayPause()
    }

    fun toggleShuffle() {
        playerRepository.toggleShuffle()
    }

    fun next() {
        viewModelScope.launch {
            playerRepository.next()
        }
    }

    fun previous() {
        viewModelScope.launch {
            playerRepository.previous()
        }
    }

    fun seekTo(positionMs: Long) {
        playerRepository.seekTo(positionMs)
    }

    fun toggleSave() {
        val currentTrack = uiState.value.track ?: return
        val currentlySaved = uiState.value.isSaved
        viewModelScope.launch {
            libraryRepository.toggleSave(currentTrack, currentlySaved)
        }
    }

    fun downloadCurrentTrack() {
        val currentTrack = uiState.value.track ?: return
        viewModelScope.launch {
            downloadRepository.download(currentTrack)
        }
    }

    fun openLyrics() {
        val currentTrack = uiState.value.track ?: return
        showLyricsSheet.value = true
        loadLyrics(currentTrack)
    }

    fun closeLyrics() {
        showLyricsSheet.value = false
    }

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
                Log.e("PlayerViewModel", "Error al añadir canción a playlist: ${e.message}", e)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                libraryRepository.createPlaylist(name)
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al crear playlist: ${e.message}", e)
            }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            try {
                libraryRepository.deletePlaylist(id)
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al eliminar playlist: ${e.message}", e)
            }
        }
    }

    private fun loadLyrics(track: Track) {
        viewModelScope.launch {
            isLoadingLyrics.value = true
            lyricsText.value = lyricsRepository.getLyrics(track)
            isLoadingLyrics.value = false
        }
    }
}
