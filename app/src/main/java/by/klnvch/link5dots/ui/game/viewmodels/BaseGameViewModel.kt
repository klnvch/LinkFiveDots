/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.ui.game.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.createPoint
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.ui.game.GameViewStateImpl
import by.klnvch.link5dots.ui.game.createGameViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseGameViewModel(
    getRoomUseCase: GetRoomUseCase,
    private val settings: Settings,
) : ViewModel(), GameActions {
    private val _focus = MutableStateFlow<Point?>(createPoint(9, 9))
    override val focus: StateFlow<Point?> = _focus

    protected val roomFlow = getRoomUseCase.room

    override val uiState = roomFlow.map { room ->
        Log.d("ViewModel", "updated: $room")
        val dotsStyleType = settings.getDotsType().first()
        createGameViewState(dotsStyleType, room)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, GameViewStateImpl())

    init {
        viewModelScope.launch {
            roomFlow.map { it.isNew }.collect {
                if (it) focus()
            }
        }
    }

    override fun focus() {
        viewModelScope.launch {
            _focus.value = roomFlow.firstOrNull()?.lastPoint
        }
    }

    override fun unfocus() {
        _focus.value = null
    }

    companion object {
        const val TAG = "ViewModel"
        const val KEY = "VIEW_MODEL_KEY"
    }
}
