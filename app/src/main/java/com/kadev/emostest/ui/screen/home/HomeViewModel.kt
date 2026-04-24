package com.kadev.emostest.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kadev.emostest.data.repository.QuoteRepository
import com.kadev.emostest.domain.error.AppError
import com.kadev.emostest.domain.model.QuoteOfTheDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class QOTDUiState {
    object Loading : QOTDUiState()
    data class Success(val data: QuoteOfTheDay) : QOTDUiState()
    data class Error(val error: AppError) : QOTDUiState()
}

sealed class SaveFavUiState {
    object Loading : SaveFavUiState()
    data class Success(val data: Long) : SaveFavUiState()
    data class Error(val error: String) : SaveFavUiState()
}

class HomeViewModel(
    private val repository: QuoteRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<QOTDUiState>(QOTDUiState.Loading)
    val uiState: StateFlow<QOTDUiState> = _uiState.asStateFlow()

    private val _saveFavUiState = MutableStateFlow<SaveFavUiState?>(null)
    val saveFavUiState: StateFlow<SaveFavUiState?> = _saveFavUiState.asStateFlow()


    fun loadQOTD() {
        viewModelScope.launch {
            _uiState.value = QOTDUiState.Loading
            repository.getQuoteOfTheDay().fold(
                onSuccess = { _uiState.value = QOTDUiState.Success(it) },
                onFailure = { _uiState.value = QOTDUiState.Error(AppError.from(it)) }
            )
        }
    }

    fun addQuoteToFavorite(quoteOfTheDay: QuoteOfTheDay) {
        viewModelScope.launch {
            val inserted = repository.insertQuoteToFavorite(quoteOfTheDay)
            _saveFavUiState.value = if (inserted != null) SaveFavUiState.Success(inserted)
                                    else SaveFavUiState.Error("Error")
        }
    }

    fun consumeSaveState() {
        _saveFavUiState.value = null
    }


}