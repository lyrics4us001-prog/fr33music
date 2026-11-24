package com.fr33music.feature.player

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.database.dao.LyricsOffsetDao
import com.fr33music.core.database.dao.PlaylistWithSongCount
import com.fr33music.core.database.dao.TrackDao
import com.fr33music.core.database.download.DownloadRepository
import com.fr33music.core.database.download.DownloadState
import com.fr33music.core.database.entity.DownloadStatus
import com.fr33music.core.database.entity.LyricsOffsetEntity
import com.fr33music.core.database.lyrics.LyricsRepository
import com.fr33music.core.database.repository.LibraryRepository
import com.fr33music.core.domain.model.Track
import com.fr33music.core.lyrics.LrcParser
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

sealed interface TranslationState {
    object Idle : TranslationState
    object Loading : TranslationState
    data class Success(
        val translatedLines: List<String> = emptyList(),
        val fullTranslatedText: String? = null
    ) : TranslationState
    data class Error(val message: String) : TranslationState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val trackDao: TrackDao,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val lyricsRepository: LyricsRepository,
    private val lyricsOffsetDao: LyricsOffsetDao
) : ViewModel() {

    private val showLyricsSheet = MutableStateFlow(false)
    private val lyricsText = MutableStateFlow<String?>(null)
    private val syncedLyricsText = MutableStateFlow<String?>(null)
    private val isLoadingLyrics = MutableStateFlow(false)
    private val lyricsOffsetMs = MutableStateFlow(0L)
    val translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)

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
        val syncedText: String? = null,
        val isLoading: Boolean = false,
        val offsetMs: Long = 0L
    )

    private val lyricsUi = combine(
        showLyricsSheet,
        lyricsText,
        syncedLyricsText,
        isLoadingLyrics,
        lyricsOffsetMs
    ) { show, text, synced, loading, offset ->
        LyricsUi(show, text, synced, loading, offset)
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
            syncedLyrics = lyrics.syncedText,
            isLoadingLyrics = lyrics.isLoading,
            showLyricsSheet = lyrics.showSheet,
            lyricsOffsetMs = lyrics.offsetMs,
            positionMs = playback.positionMs,
            durationMs = if (playback.durationMs > 0L) playback.durationMs else (track?.durationMs ?: 0L),
            error = playback.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    data class LyricsPlaybackState(
        val syncedText: String? = null,
        val plainText: String? = null,
        val positionMs: Long = 0L,
        val offsetMs: Long = 0L
    )

    val lyricsPlaybackState: StateFlow<LyricsPlaybackState> = combine(
        syncedLyricsText,
        lyricsText,
        playerRepository.playbackState,
        lyricsOffsetMs
    ) { synced, plain, playback, offset ->
        LyricsPlaybackState(
            syncedText = synced,
            plainText = plain,
            positionMs = playback.positionMs,
            offsetMs = offset
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LyricsPlaybackState()
    )

    val playerPosition: StateFlow<Long> = playerRepository.playbackState
        .map { it.positionMs }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    init {
        viewModelScope.launch {
            currentTrackId.collect { trackId ->
                if (trackId != null) {
                    val savedOffset = try {
                        lyricsOffsetDao.getOffsetForTrack(trackId) ?: 0L
                    } catch (e: Exception) {
                        Log.e("PlayerViewModel", "Error al leer offset de Room: ${e.message}", e)
                        0L
                    }
                    lyricsOffsetMs.value = savedOffset
                } else {
                    lyricsOffsetMs.value = 0L
                }

                if (showLyricsSheet.value) {
                    val track = playerRepository.playbackState.value.currentTrack
                    if (track != null) {
                        loadLyrics(track)
                    }
                }
            }
        }
    }

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

    fun adjustLyricsOffset(deltaMs: Long) {
        val newOffset = lyricsOffsetMs.value + deltaMs
        lyricsOffsetMs.value = newOffset
        val currentTrack = uiState.value.track ?: return
        viewModelScope.launch {
            try {
                lyricsOffsetDao.insertOrUpdateOffset(
                    LyricsOffsetEntity(
                        videoId = currentTrack.trackId,
                        offsetMs = newOffset
                    )
                )
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al guardar offset de letra: ${e.message}", e)
            }
        }
    }

    fun resetLyricsOffset() {
        lyricsOffsetMs.value = 0L
        val currentTrack = uiState.value.track ?: return
        viewModelScope.launch {
            try {
                lyricsOffsetDao.insertOrUpdateOffset(
                    LyricsOffsetEntity(
                        videoId = currentTrack.trackId,
                        offsetMs = 0L
                    )
                )
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error al reiniciar offset de letra: ${e.message}", e)
            }
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

    fun translateLyrics(context: Context) {
        val currentTrack = uiState.value.track ?: return
        val currentLyrics = uiState.value.lyrics
        val synced = uiState.value.syncedLyrics

        if (translationState.value is TranslationState.Loading) return

        if (translationState.value is TranslationState.Success) {
            // Si ya está traducido, al tocar nuevamente se desactiva (toggle)
            translationState.value = TranslationState.Idle
            return
        }

        viewModelScope.launch {
            translationState.value = TranslationState.Loading

            val lines = if (!synced.isNullOrBlank()) {
                LrcParser.parseLrc(synced).map { it.text }
            } else if (!currentLyrics.isNullOrBlank()) {
                currentLyrics.lines().map { it.trim() }.filter { it.isNotBlank() }
            } else {
                emptyList()
            }

            if (lines.isEmpty()) {
                translationState.value = TranslationState.Idle
                Toast.makeText(context, "No hay letra disponible para traducir", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val result = lyricsRepository.translateLines(lines)
            if (result != null && result.isNotEmpty()) {
                translationState.value = TranslationState.Success(
                    translatedLines = result,
                    fullTranslatedText = result.joinToString("\n")
                )
            } else {
                translationState.value = TranslationState.Error("Se necesita conexión a internet para traducir")
                Toast.makeText(context, "Se necesita conexión a internet para traducir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadLyrics(track: Track) {
        viewModelScope.launch {
            translationState.value = TranslationState.Idle
            isLoadingLyrics.value = true
            val result = lyricsRepository.getLyrics(track)
            lyricsText.value = result.plainText
            syncedLyricsText.value = result.syncedText
            isLoadingLyrics.value = false
        }
    }
}
