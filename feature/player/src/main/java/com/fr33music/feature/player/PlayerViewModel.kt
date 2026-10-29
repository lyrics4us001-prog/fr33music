package com.fr33music.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fr33music.core.player.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerRepository: PlayerRepository
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = playerRepository.playbackState
        .map { state ->
            val track = state.currentTrack
            PlayerUiState(
                track = track,
                isPlaying = state.isPlaying,
                isLoading = state.isLoading,
                positionMs = state.positionMs,
                durationMs = track?.durationMs ?: 0L,
                error = state.error
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerUiState()
        )

    fun togglePlayPause() {
        playerRepository.togglePlayPause()
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
}
