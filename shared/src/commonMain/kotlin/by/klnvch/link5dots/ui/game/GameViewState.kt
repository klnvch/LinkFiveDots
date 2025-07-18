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

import by.klnvch.link5dots.domain.models.ActionAvailability
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.WinningLine
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameViewState {
    val infoViewState: GameInfoViewState
    val boardViewState: GameBoardViewState
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameInfoViewState {
    val dotsStyleType: DotsStyleType
    val user1Name: String?
    val user2Name: String?
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameBoardViewState {
    val dotsStyleType: DotsStyleType
    val isNew: Boolean
    val dots: Array<Dot>
    val winningLine: WinningLine?
    val lastDot: Dot?
}

fun createGameViewState(
    dotsStyleType: DotsStyleType,
    user1Name: String?,
    user2Name: String?,
    room: IRoom?,
): GameViewState = GameViewStateImpl(
    GameInfoViewStateImpl(dotsStyleType, user1Name, user2Name),
    GameBoardViewStateImpl(
        dotsStyleType,
        room?.isNew() != false,
        room?.dots?.toTypedArray() ?: emptyArray(),
        room?.getWinningLine(),
    ),
)

data class GameViewStateImpl(
    override val infoViewState: GameInfoViewState = GameInfoViewStateImpl(),
    override val boardViewState: GameBoardViewState = GameBoardViewStateImpl(),
) : GameViewState

class GameInfoViewStateImpl(
    override val dotsStyleType: DotsStyleType = DotsStyleType.ORIGINAL,
    override val user1Name: String? = null,
    override val user2Name: String? = null,
) : GameInfoViewState

class GameBoardViewStateImpl(
    override val dotsStyleType: DotsStyleType = DotsStyleType.ORIGINAL,
    override val isNew: Boolean = true,
    override val dots: Array<Dot> = emptyArray(),
    override val winningLine: WinningLine? = null,
) : GameBoardViewState {
    override val lastDot = dots.lastOrNull()
}

data class MenuViewState(
    val newGameAvailability: ActionAvailability = ActionAvailability.Gone,
    val isUndoSupported: Boolean = false,
    val isUndoAvailable: Boolean = false,
)
