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
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.WinningLine
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalJsExport::class)
@JsExport
interface GameViewState {
    val infoViewState: GameInfoViewState
    val boardViewState: GameBoardViewState
    val menuViewState: MenuViewState
    val isOver: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface GameInfoUserViewState {
    val name: String
    val duration: Int
    val canMove: Boolean
    val isWon: Boolean
    val time: Int?
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface GameInfoViewState {
    val dotsStyleType: DotsStyleType
    val user1: GameInfoUserViewState
    val user2: GameInfoUserViewState
    val size: String
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface GameBoardViewState {
    val dotsStyleType: DotsStyleType
    val dots: Array<Dot>
    val winningLine: WinningLine?
    val lastDot: Dot?
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface MenuViewState {
    val newOption: ActionAvailability
    val undoOption: ActionAvailability
    val shareOption: ActionAvailability
}

fun createGameViewState(
    dotsStyleType: DotsStyleType,
    gameState: GameState,
): GameViewState {
    val lastDotTime = gameState.lastDotTime
    val user1CanMove = gameState.user1.canMove
    val user2CanMove = gameState.user2.canMove

    return GameViewStateImpl(
        GameInfoViewStateImpl(
            dotsStyleType,
            GameInfoUserViewStateImpl(
                gameState.user1.name ?: "",
                gameState.user1.duration,
                user1CanMove,
                gameState.user1.isWon,
                if (user1CanMove) lastDotTime else null,
            ),
            GameInfoUserViewStateImpl(
                gameState.user2.name ?: "",
                gameState.user2.duration,
                user2CanMove,
                gameState.user2.isWon,
                if (user2CanMove) lastDotTime else null,
            ),
            gameState.size.toString(),
        ),
        GameBoardViewStateImpl(
            dotsStyleType,
            gameState.dots.toTypedArray(),
            gameState.winningLine,
        ),
        MenuViewStateImpl(
            gameState.gameActions.newAction,
            gameState.gameActions.undoAction,
            gameState.gameActions.shareAction,
        ),
        gameState.isOver,
    )
}

data class GameViewStateImpl(
    override val infoViewState: GameInfoViewState = GameInfoViewStateImpl(),
    override val boardViewState: GameBoardViewState = GameBoardViewStateImpl(),
    override val menuViewState: MenuViewState = MenuViewStateImpl(),
    override val isOver: Boolean = false,
) : GameViewState

data class GameInfoUserViewStateImpl(
    override val name: String = "",
    override val duration: Int = 0,
    override val canMove: Boolean = false,
    override val isWon: Boolean = false,
    override val time: Int? = null,
) : GameInfoUserViewState

class GameInfoViewStateImpl(
    override val dotsStyleType: DotsStyleType = DotsStyleType.ORIGINAL,
    override val user1: GameInfoUserViewState = GameInfoUserViewStateImpl(),
    override val user2: GameInfoUserViewState = GameInfoUserViewStateImpl(),
    override val size: String = 0.toString(),
) : GameInfoViewState

class GameBoardViewStateImpl(
    override val dotsStyleType: DotsStyleType = DotsStyleType.ORIGINAL,
    override val dots: Array<Dot> = emptyArray(),
    override val winningLine: WinningLine? = null,
) : GameBoardViewState {
    override val lastDot = dots.lastOrNull()
}

data class MenuViewStateImpl(
    override val newOption: ActionAvailability = ActionAvailability.Gone,
    override val undoOption: ActionAvailability = ActionAvailability.Gone,
    override val shareOption: ActionAvailability = ActionAvailability.Gone,
) : MenuViewState

private fun Int.formatDurationPart() = if (this < 10) "0${this}" else toString()

@OptIn(ExperimentalJsExport::class)
@JsExport
fun Int.formatDuration() =
    if (this > 0) seconds.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) "${hours}:${minutes.formatDurationPart()}:${seconds.formatDurationPart()}"
        else "${minutes.formatDurationPart()}:${seconds.formatDurationPart()}"
    } else ""
