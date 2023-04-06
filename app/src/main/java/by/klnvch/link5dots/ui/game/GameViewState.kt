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

import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.WinningLine

data class GameViewState(
    val infoViewState: GameInfoViewState,
    val boardViewState: GameBoardViewState,
) {
    constructor(
        dotsStyleType: DotsStyleType,
        user1Name: String?,
        user2Name: String?,
        room: IRoom?,
    ) : this(
        GameInfoViewState(dotsStyleType, user1Name, user2Name),
        GameBoardViewState(
            dotsStyleType,
            room?.dots?.toMutableList() ?: emptyList<Dot>(),
            room?.getWinningLine(),
        ),
    )

    companion object {
        fun default() = GameViewState(
            DotsStyleType.ORIGINAL,
            null,
            null,
            null,
        )
    }
}

data class GameInfoViewState(
    val dotsStyleType: DotsStyleType,
    val user1Name: String?,
    val user2Name: String?,
) {
    val user1Dot = when (dotsStyleType) {
        DotsStyleType.ORIGINAL -> R.drawable.game_dot_circle_red
        DotsStyleType.CROSS_AND_RING -> R.drawable.game_dot_cross_red
    }

    val user2Dot = when (dotsStyleType) {
        DotsStyleType.ORIGINAL -> R.drawable.game_dot_circle_blue
        DotsStyleType.CROSS_AND_RING -> R.drawable.game_dot_ring_blue
    }
}

data class GameBoardViewState(
    val dotsStyleType: DotsStyleType,
    val dots: List<Dot>,
    val winningLine: WinningLine?,
) {
    val isOver = winningLine != null
    val lastDot = dots.lastOrNull()
}
