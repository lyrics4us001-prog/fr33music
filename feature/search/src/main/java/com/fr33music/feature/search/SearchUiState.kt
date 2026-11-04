package com.fr33music.feature.search

import com.fr33music.core.domain.model.LookingSearchResult
import com.fr33music.core.domain.model.Track

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val tracks: List<Track>) : SearchUiState
    data class LookingSuccess(val result: LookingSearchResult) : SearchUiState
    data object Empty : SearchUiState
    data class Error(val message: String) : SearchUiState
}
