/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package by.klnvch.link5dots.ui.scores

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.di.viewmodels.AssistedSavedStateViewModelFactory
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.usecases.*
import by.klnvch.link5dots.ui.scores.history.HistoryItemViewState
import by.klnvch.link5dots.ui.scores.history.HistoryViewState
import by.klnvch.link5dots.ui.scores.scores.FirebaseState
import by.klnvch.link5dots.ui.scores.scores.ScoresViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScoresViewModel @AssistedInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val deleteRoomUseCase: DeleteRoomUseCase,
    private val saveRoomUseCase: SaveRoomUseCase,
    private val getRoomsUseCase: GetRoomsUseCase,
    private val getScorePathUseCase: GetScorePathUseCase,
    private val getUserNameUseCase: GetUserNameUseCase,
) : ViewModel() {
    private val _historyUiState = MutableStateFlow(HistoryViewState.initial())
    val historyUiState: StateFlow<HistoryViewState> = _historyUiState

    private val _scoresUiState = MutableStateFlow(ScoresViewState.initial())
    val scoresUiState: StateFlow<ScoresViewState> = _scoresUiState

    init {
        viewModelScope.launch {
            getRoomsUseCase.get().collect { rooms ->
                _historyUiState.value = HistoryViewState(rooms.map {
                    val user1Name = getUserNameUseCase.get(it.user1)
                    val user2Name = getUserNameUseCase.get(it.user2)
                    HistoryItemViewState(it, user1Name, user2Name)
                })
            }
        }

        viewModelScope.launch {
            try {
                val scorePath = getScorePathUseCase.signInAndGet()
                _scoresUiState.value = ScoresViewState(FirebaseState.SIGNED_IN, scorePath)
            } catch (e: NotSupportedError) {
                _scoresUiState.value = ScoresViewState(FirebaseState.NOT_SUPPORTED)
            } catch (e: Error) {
                _scoresUiState.value = ScoresViewState(FirebaseState.ERROR)
            }
        }
    }

    fun getCurrentItem(): Int {
        return savedStateHandle.get<Int>(CURRENT_TAB_POSITION_KEY) ?: ScoresTabPosition.SCORES
    }

    fun setCurrentItem(currentItem: Int) {
        savedStateHandle[CURRENT_TAB_POSITION_KEY] = currentItem
    }

    fun deleteRoom(room: IRoom) {
        viewModelScope.launch {
            deleteRoomUseCase.delete(room)
        }
    }

    fun insertRoom(room: IRoom) {
        viewModelScope.launch {
            saveRoomUseCase.save(room)
        }
    }

    fun signOut() {
        getScorePathUseCase.signOut()
    }

    companion object {
        private const val CURRENT_TAB_POSITION_KEY = "currentTabPosition"
    }

    @AssistedFactory
    interface Factory : AssistedSavedStateViewModelFactory<ScoresViewModel> {
        override fun create(savedStateHandle: SavedStateHandle): ScoresViewModel
    }
}
