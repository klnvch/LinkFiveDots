/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.findWinningLine
import by.klnvch.link5dots.domain.repositories.AddDotOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.UpdateStateOnlineRoomRepository

interface AddDotUseCase {
    // TODO: remove room as param
    suspend fun addDot(room: IRoom, p: Point)
}

class AddDotOnlineUseCase(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val board: Board,
    private val addDotRepository: AddDotOnlineRoomRepository,
    private val updateStateRepository: UpdateStateOnlineRoomRepository,
    private val getRepository: GetOnlineRoomRepository,
) : AddDotUseCase {
    override suspend fun addDot(room: IRoom, p: Point) {
        getRepository.room?.let { room ->
            if (board.isInside(p) && room.isFree(p) && room.isNotOver()) {
                val userId = firebaseAuthManager.getUserId()

                val type = if (room.user1.id == userId && room.dots.size % 2 == 0) {
                    Dot.HOST
                } else if (room.user2?.id == userId && room.dots.size % 2 == 1) {
                    Dot.GUEST
                } else {
                    null
                }

                type?.let { type ->
                    val dot = DotImpl(p, type, 0)
                    addDotRepository.addDot(room.key, room.dots.size, p)
                    if ((room.dots + dot).findWinningLine() != null) {
                        updateStateRepository.update(room.key, RoomState.FINISHED)
                    }
                }
            }
        }
    }
}
