package com.kadev.emostest.ui.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kadev.emostest.data.repository.QuoteRepository
import com.kadev.emostest.domain.error.AppError
import com.kadev.emostest.domain.model.QuoteOfTheDay
import com.kadev.emostest.ui.screen.home.QOTDUiState
import com.kadev.emostest.ui.screen.home.SaveFavUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FavQuoteUiState {
    object Loading : FavQuoteUiState()
    data class Success(val data: List<QuoteOfTheDay>) : FavQuoteUiState()
    data class Error(val error: String) : FavQuoteUiState()
}

sealed class DelQuoteUiState {
    object Loading : DelQuoteUiState()
    data class Success(val data:Int) : DelQuoteUiState()
    data class Error(val error: String) : DelQuoteUiState()
}

class FavoriteQuoteViewModel(
    private val repository: QuoteRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<FavQuoteUiState>(FavQuoteUiState.Loading)
    val uiState: StateFlow<FavQuoteUiState> = _uiState.asStateFlow()

    private val _delUiState = MutableStateFlow<DelQuoteUiState?>(null)
    val delUiState: StateFlow<DelQuoteUiState?> = _delUiState.asStateFlow()

    fun loadFav() {
        viewModelScope.launch {
            _uiState.value = FavQuoteUiState.Loading
            val listFav = repository.getAllFavorite()
            if(listFav.isNotEmpty()){
                _uiState.value = FavQuoteUiState.Success(listFav)
            }else{
                _uiState.value = FavQuoteUiState.Error("Error or Empty")
            }
        }
    }

    fun deleteFav(quoteOfTheDay: QuoteOfTheDay){
        viewModelScope.launch {
            _delUiState.value = DelQuoteUiState.Loading
            val deletedId = repository.deleteFav(quoteOfTheDay)
            if(deletedId != null){
                _delUiState.value = DelQuoteUiState.Success(deletedId)
                loadFav()
                _delUiState.value = null
            }else{
                _delUiState.value = DelQuoteUiState.Error("Error")
            }
        }
    }
}