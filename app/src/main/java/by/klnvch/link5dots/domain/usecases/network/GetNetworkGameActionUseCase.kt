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
package by.klnvch.link5dots.domain.usecases.network

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkGameAction
import by.klnvch.link5dots.domain.models.NetworkRoomExtended
import by.klnvch.link5dots.domain.models.RoomState
import javax.inject.Inject

class GetNetworkGameActionUseCase @Inject constructor() {
    fun get(room: IRoom): NetworkGameAction {
        return if (room is NetworkRoomExtended)
            if (room.user1.id == room.yourId) map(room, 0)
            else if (room.user2?.id == room.yourId) map(room, 1)
            else NetworkGameAction.UNKNOWN
        else
            throw IllegalStateException("Wrong room type")
    }

    private fun map(room: NetworkRoomExtended, expectedOrder: Int): NetworkGameAction {
        val order = room.dots.size % 2
        return if (room.isOver())
            if (order == expectedOrder) NetworkGameAction.GAME_OVER_LOSE
            else NetworkGameAction.GAME_OVER_WIN
        else if (room.state == RoomState.FINISHED) NetworkGameAction.DISCONNECTED
        else if (order == expectedOrder) NetworkGameAction.MOVE
        else NetworkGameAction.WAIT
    }
}
