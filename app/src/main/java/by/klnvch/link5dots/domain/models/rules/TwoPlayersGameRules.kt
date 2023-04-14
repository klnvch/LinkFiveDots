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
import javax.inject.Inject

class TwoPlayersGameRules @Inject constructor(
    roomKeyGenerator: RoomKeyGenerator,
    initialGameGenerator: InitialGameGenerator,
    board: Board,
) : GameRules(roomKeyGenerator, initialGameGenerator, board) {
    override fun init(room: IRoom?): Room {
        if (room != null) {
            this.room = Room(room).copy(user1 = null, user2 = null)
        } else {
            this.room = createGame()
        }
        return this.room
    }

    override fun addInternal(p: Point) {
        val lastDotType = room.dots.lastOrNull()?.type ?: Dot.GUEST
        val type = if (lastDotType == Dot.GUEST) Dot.HOST else Dot.GUEST
        val dt = (System.currentTimeMillis() - room.timestamp).toInt()
        room.add(Dot(p, type, dt))
    }

    override fun undo(): Room {
        room.undo()
        return room
    }

    override fun createGame(): Room = Room(
        roomKeyGenerator.get(),
        System.currentTimeMillis(),
        mutableListOf(),
        null,
        null,
        RoomType.TWO_PLAYERS,
    )

    override fun getScore(): SimpleGameScore {
        return SimpleGameScore(
            room.dots.size,
            room.getDuration(),
            room.getEndTime(),
        )
    }
}
