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
package by.klnvch.link5dots.ui.game.viewmodels

import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.application.services.gameRoomOrchestrator.GameRoomOrchestrator
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

open class OfflineGameViewModel @Inject constructor(
    getRoomUseCase: GameRoomOrchestrator,
    private val newGameUseCase: NewGameUseCase,
    private val addDotUseCase: AddDotUseCase,
    private val undoMoveUseCase: UndoMoveUseCase,
    private val saveScoreUseCase: SaveScoreUseCase,
) : BaseGameViewModel(getRoomUseCase) {

    override fun undo() {
        viewModelScope.launch { undoMoveUseCase.undo() }
    }

    override fun new(): Boolean {
        viewModelScope.launch { newGameUseCase.create() }
        return newGameUseCase.isImplemented
    }

    override fun addDot(p: Point) {
        viewModelScope.launch { addDotUseCase.addDot(p) }
    }

    override fun saveScore() {
        viewModelScope.launch { saveScoreUseCase.save() }
    }
}
