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
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.RoomState

object RoomToTitleMapper {
    fun roomToTitle(room: NetworkRoom, userId: String): Int {
        return if (room.user1.id == userId) {
            map(room, 0)
        } else if (room.user2?.id == userId) {
            map(room, 1)
        } else {
            0
        }
    }

    private fun map(room: NetworkRoom, expectedOrder: Int): Int {
        val order = room.dots.size % 2
        return if (room.isOver()) {
            if (order == expectedOrder) R.string.end_lose
            else R.string.end_win
        } else if (room.state == RoomState.FINISHED) {
            R.string.disconnected
        } else {
            if (order == expectedOrder) R.string.bt_message_your_turn
            else R.string.bt_message_opponents_turn
        }
    }
}
