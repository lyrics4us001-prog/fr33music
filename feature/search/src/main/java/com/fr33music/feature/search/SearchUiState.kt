package com.fr33music.feature.search

import com.fr33music.core.domain.model.Track

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val tracks: List<Track>) : SearchUiState
    object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}
