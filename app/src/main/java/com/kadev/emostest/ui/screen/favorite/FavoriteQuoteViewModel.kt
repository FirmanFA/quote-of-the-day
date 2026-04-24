package com.kadev.emostest.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kadev.emostest.data.repository.QuoteRepository
import com.kadev.emostest.domain.model.QuoteOfTheDay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class FavQuoteUiState {
    object Loading : FavQuoteUiState()
    data class Success(val data: List<QuoteOfTheDay>) : FavQuoteUiState()
    data class Error(val error: String) : FavQuoteUiState()
}

class FavoriteQuoteViewModel(
    private val repository: QuoteRepository
) : ViewModel() {

    val uiState: StateFlow<FavQuoteUiState> = repository.getAllFavorite()
        .map<List<QuoteOfTheDay>, FavQuoteUiState> { FavQuoteUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavQuoteUiState.Loading
        )

    fun deleteFav(quoteOfTheDay: QuoteOfTheDay) {
        viewModelScope.launch {
            repository.deleteFav(quoteOfTheDay)
        }
    }
}