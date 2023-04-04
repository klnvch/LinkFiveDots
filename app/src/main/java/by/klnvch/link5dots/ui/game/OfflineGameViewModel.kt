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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.GameRules
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.usecases.GetRecentRoomUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.ui.game.create.NewGameViewState
import by.klnvch.link5dots.ui.game.end.EndGameViewState
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class OfflineGameViewModel @Inject constructor(
    private val gameRules: GameRules,
    private val getRecentRoomUseCase: GetRecentRoomUseCase,
    private val analytics: Analytics,
    private val saveScoreUseCase: SaveScoreUseCase,
) : ViewModel() {
    private val _room: MutableLiveData<IRoom?> = MutableLiveData()
    val room: LiveData<IRoom?> = _room

    init {
        viewModelScope.launch {
            val room = getRecentRoomUseCase.get(gameRules.type)
            _room.value = gameRules.init(room)
        }
    }

    fun undoLastMove() {
        analytics.logEvent(Analytics.EVENT_UNDO_MOVE)
        _room.value = gameRules.undo()
    }

    fun newGame(seed: Long?) {
        analytics.logEvent(if (seed != null) Analytics.EVENT_GENERATE_GAME else Analytics.EVENT_NEW_GAME)
        _room.value = gameRules.newGame(seed)
    }

    fun addDot(dot: Point) {
        analytics.logEvent(Analytics.EVENT_NEW_MOVE)
        _room.value = gameRules.addDot(dot)
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
