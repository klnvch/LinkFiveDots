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
import by.klnvch.link5dots.domain.models.getDuration
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameViewState {
    val infoViewState: GameInfoViewState
    val boardViewState: GameBoardViewState
    val menuViewState: MenuViewState
    val showNextActions: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameInfoUserViewState {
    val name: String
    val duration: String
    val canMove: Boolean
    val isWon: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameInfoViewState {
    val dotsStyleType: DotsStyleType
    val user1: GameInfoUserViewState
    val user2: GameInfoUserViewState
    val size: String
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameBoardViewState {
    val dotsStyleType: DotsStyleType
    val dots: Array<Dot>
    val winningLine: WinningLine?
    val lastDot: Dot?
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface MenuViewState {
    val newOption: ActionAvailability
    val undoOption: ActionAvailability
    val shareOption: ActionAvailability
}

fun createGameViewState(
    dotsStyleType: DotsStyleType,
    user1Name: String?,
    user2Name: String?,
    room: IRoom?,
    newActionAvailability: ActionAvailability,
    undoActionAvailability: ActionAvailability,
    shareActionAvailability: ActionAvailability,
): GameViewState = GameViewStateImpl(
    GameInfoViewStateImpl(
        dotsStyleType,
        GameInfoUserViewStateImpl(
            user1Name ?: "",
            room?.dots.getDuration(1).formatDuration(),
            room.canMove(0),
            room.isWon(1),
        ),
        GameInfoUserViewStateImpl(
            user2Name ?: "",
            room?.dots.getDuration(0).formatDuration(),
            room.canMove(1),
            room.isWon(0),
        ),
        (room?.dots?.size ?: 0).toString(),
    ),
    GameBoardViewStateImpl(
        dotsStyleType,
        room?.dots?.toTypedArray() ?: emptyArray(),
        room?.getWinningLine(),
    ),
    MenuViewStateImpl(
        newActionAvailability,
        undoActionAvailability,
        shareActionAvailability,
    ),
    room?.isOver() == true && (newActionAvailability.isEnabled || undoActionAvailability.isEnabled || shareActionAvailability.isEnabled),
)

data class GameViewStateImpl(
    override val infoViewState: GameInfoViewState = GameInfoViewStateImpl(),
    override val boardViewState: GameBoardViewState = GameBoardViewStateImpl(),
    override val menuViewState: MenuViewState = MenuViewStateImpl(),
    override val showNextActions: Boolean = false,
) : GameViewState

data class GameInfoUserViewStateImpl(
    override val name: String = "",
    override val duration: String = "",
    override val canMove: Boolean = false,
    override val isWon: Boolean = false,
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

private fun Int.formatDuration() =
    if (this > 0) seconds.toComponents { hours, minutes, seconds, _ ->
        if (hours > 0) "${hours}:${minutes.formatDurationPart()}:${seconds.formatDurationPart()}"
        else "${minutes.formatDurationPart()}:${seconds.formatDurationPart()}"
    } else ""

private fun IRoom?.canMove(n: Int) =
    if (this == null || this.isOver()) false else dots.size % 2 == n

private fun IRoom?.isWon(n: Int) =
    if (this == null || this.isNotOver()) false else dots.size % 2 == n
