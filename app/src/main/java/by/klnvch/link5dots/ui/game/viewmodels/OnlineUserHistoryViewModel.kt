package by.klnvch.link5dots.ui.game.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.history.entities.OnlineGameShortInfo
import by.klnvch.link5dots.domain.history.usecase.GetOnlineUserHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UserHistoryViewState {
    object Loading : UserHistoryViewState()
    object Empty : UserHistoryViewState()
    data class Ready(val items: List<OnlineGameShortInfo>) : UserHistoryViewState()
}

class OnlineUserHistoryViewModel @Inject constructor(
    private val userHistoryUseCase: GetOnlineUserHistoryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UserHistoryViewState>(UserHistoryViewState.Loading)
    val uiState: StateFlow<UserHistoryViewState> = _uiState

    fun load() {
        viewModelScope.launch {
            val history = userHistoryUseCase.getUserHistory()
            if (history.items.isEmpty()) {
                _uiState.value = UserHistoryViewState.Empty
            } else {
                _uiState.value = UserHistoryViewState.Ready(history.items)
            }
        }
    }

    companion object {
        const val KEY = "OnlineUserHistoryViewModel"
    }
}
