/*
 * MIT License
 *
 * Copyright (c) 2025-2026 klnvch
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

package by.klnvch.link5dots.domain.models

fun List<Dot>?.getDuration(d: Int) = this
    ?.drop(d)
    ?.chunked(2)
    ?.filter { it.size > 1 }
    ?.filter { it[0].dt > 0 && it[1].dt > 0 }
    ?.sumOf { it[1].dt - it[0].dt }
    ?: 0

data class UserState(
    val name: String?,
    val canMove: Boolean,
    val isWon: Boolean,
    val duration: Int,
)

data class GameState(
    val room: IRoom,
    val gameActions: GameActions,
    private val userNames: ResolvedUserNames,
    private val isActive: Boolean,
) {
    val lastDotTime: Int? = room.let {
        val dt = it.dots.lastOrNull()?.dt ?: 0
        if (dt > 0) room.time + dt else null
    }
    val isOver: Boolean = room.isOver() || !isActive
    val isNew: Boolean = room.isNew()
    val lastPoint: Point? = room.lastPoint()
    val dots: List<Dot> = room.dots
    val winningLine: WinningLine? = room.getWinningLine()
    val size: Int = room.dots.size
    val user1 = UserState(
        userNames.user1Name,
        isActive && room.canMove(0),
        room.isWon(1),
        room.dots.getDuration(1),
    )
    val user2 = UserState(
        userNames.user2Name,
        isActive && room.canMove(1),
        room.isWon(0),
        room.dots.getDuration(0),
    )
}
