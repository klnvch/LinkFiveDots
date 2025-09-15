/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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
package by.klnvch.link5dots.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.gameSeed
import by.klnvch.link5dots.domain.models.isNew
import by.klnvch.link5dots.domain.models.lastPoint
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GameActionsUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.GetUserNameUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreUseCase
import by.klnvch.link5dots.domain.usecases.RoomParam
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

open class OfflineGameViewModel @Inject constructor(
    private val getRoomUseCase: GetRoomUseCase,
    private val newGameUseCase: NewGameUseCase,
    private val addDotUseCase: AddDotUseCase,
    private val undoMoveUseCase: UndoMoveUseCase,
    private val prepareScoreUseCase: PrepareScoreUseCase,
    private val saveScoreUseCase: SaveScoreUseCase,
    private val settings: Settings,
    private val getUserNameUseCase: GetUserNameUseCase,
    private val getGameActionsUseCase: GameActionsUseCase,
) : ViewModel() {
    private val _searchQueryFlow = MutableSharedFlow<RoomParam>(1)

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val roomFlowGuard = _searchQueryFlow.flatMapLatest { getRoomUseCase.get(it) }

    protected val roomFlow = roomFlowGuard.filterNotNull()

    val uiState = roomFlow.map {
        val type = settings.getDotsType().first()
        val user1Name = getUserNameUseCase.get(it.user1)
        val user2Name = getUserNameUseCase.get(it.user2)
        val newActionAvailability = getGameActionsUseCase.getNewAvailability()
        val undoActionAvailability = getGameActionsUseCase.getUndoAvailability()
        val shareActionAvailability = getGameActionsUseCase.getShareAvailability()
        createGameViewState(
            type,
            user1Name,
            user2Name,
            it,
            newActionAvailability,
            undoActionAvailability,
            shareActionAvailability,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GameViewStateImpl())

    private val _focus = MutableStateFlow<Point?>(Point(9, 9))
    val focus: StateFlow<Point?> = _focus

    init {
        viewModelScope.launch {
            roomFlowGuard.collect {
                if (it === null) {
                    newGameUseCase.create(gameSeed())
                }
            }
        }
        viewModelScope.launch {
            roomFlow.collect {
                if (it.isNew()) {
                    focus()
                }
            }
        }
    }

    fun setParam(param: RoomParam) = viewModelScope.launch { _searchQueryFlow.emit(param) }

    fun undoLastMove() = viewModelScope.launch { undoMoveUseCase.undo() }

    fun newGame(): Boolean {
        viewModelScope.launch {
            newGameUseCase.create(gameSeed())
        }
        return newGameUseCase.isImplemented
    }

    fun addDot(p: Point) = viewModelScope.launch { addDotUseCase.addDot(p) }

    fun saveScore() = viewModelScope.launch {
        val room = roomFlow.firstOrNull()
        if (room != null) {
            val score = prepareScoreUseCase.get(room)
            saveScoreUseCase.save(score as BotGameScore)
        }
    }

    fun focus() = viewModelScope.launch {
        _focus.value = roomFlow.firstOrNull()?.lastPoint()
    }

    fun unfocus() {
        _focus.value = null
    }

    companion object {
        const val TAG = "ViewModel"
        const val KEY = "VIEW_MODEL_KEY"
    }
}
