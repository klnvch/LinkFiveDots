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

package by.klnvch.link5dots.domain.models.rules

import by.klnvch.link5dots.domain.models.*

abstract class GameRules(
    protected val roomKeyGenerator: RoomKeyGenerator,
    private val initialGameGenerator: InitialGameGenerator,
    private val board: Board,
) {
    protected lateinit var room: Room

    abstract fun init(room: IRoom?): Room

    fun addDot(p: Point): Room? {
        if (board.isInside(p) && room.isFree(p) && room.isNotOver()) {
            addInternal(p)
            return room
        }
        return null
    }

    abstract fun addInternal(p: Point)

    abstract fun undo(): Room

    protected abstract fun createGame(): Room

    fun newGame(seed: Long?): Room {
        this.room = createGame()
        generateDots(seed)
        return room
    }

    abstract fun getScore(): SimpleGameScore

    private fun generateDots(seed: Long?) {
        if (seed != null) {
            val tp = Point(8, 8)
            initialGameGenerator.get(seed)
                .map { it.translate(tp) }
                .withIndex()
                .forEach { (i, p) ->
                    val type = if (i % 2 == 0) Dot.HOST else Dot.GUEST
                    val dt = (System.currentTimeMillis() - room.timestamp).toInt()
                    room.add(Dot(p, type, dt))
                }
        }
    }
}
