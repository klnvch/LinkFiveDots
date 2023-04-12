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

package by.klnvch.link5dots.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.rules.GameRules
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.GetUserNameUseCase
import by.klnvch.link5dots.domain.usecases.SaveRoomUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.room.GetRoomUseCase
import by.klnvch.link5dots.ui.game.create.NewGameViewState
import by.klnvch.link5dots.ui.game.end.EndGameViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class OfflineGameViewModel @Inject constructor(
    private val gameRules: GameRules,
    private val getRoomUseCase: GetRoomUseCase,
    private val saveRoomUseCase: SaveRoomUseCase,
    private val analytics: Analytics,
    private val saveScoreUseCase: SaveScoreUseCase,
    private val settings: Settings,
    private val getUserNameUseCase: GetUserNameUseCase,
) : ViewModel() {
    private val searchQueryFlow = MutableSharedFlow<GetRoomUseCase.RoomParam>(1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = searchQueryFlow
        .flatMapLatest {
            getRoomUseCase.get(it).map { room ->
                val type = settings.getDotsType().first()
                val user1Name = getUserNameUseCase.get(room?.user1)
                val user2Name = getUserNameUseCase.get(room?.user2)
                GameViewState(type, user1Name, user2Name, gameRules.init(room))
            }
        }.asLiveData()

    fun setParam(param: GetRoomUseCase.RoomParam) {
        viewModelScope.launch { searchQueryFlow.emit(param) }
    }

    fun undoLastMove() {
        analytics.logEvent(Analytics.EVENT_UNDO_MOVE)

        viewModelScope.launch {
            val room = gameRules.undo()
            saveRoomUseCase.save(room)
        }
    }

    fun newGame(seed: Long?) {
        analytics.logEvent(if (seed != null) Analytics.EVENT_GENERATE_GAME else Analytics.EVENT_NEW_GAME)

        viewModelScope.launch {
            val room = gameRules.newGame(seed)
            saveRoomUseCase.save(room)
        }
    }

    fun addDot(dot: Point) {
        analytics.logEvent(Analytics.EVENT_NEW_MOVE)
        viewModelScope.launch {
            val room = gameRules.addDot(dot)
            saveRoomUseCase.save(room)
        }
    }

    fun getEndGameViewState(): EndGameViewState {
        val score = gameRules.getScore()
        return EndGameViewState(score)
    }

    fun getNewGameViewState(): NewGameViewState {
        val seed = Random().nextInt(0xFFFF)
        return NewGameViewState(seed.toString())
    }

    fun saveScore() {
        val score = gameRules.getScore()
        analytics.logEvent(Analytics.EVENT_GAME_FINISHED)
        viewModelScope.launch {
            saveScoreUseCase.save(score as BotGameScore)
        }
    }
}
