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
import by.klnvch.link5dots.domain.models.Bot
import by.klnvch.link5dots.domain.models.BotUser
import by.klnvch.link5dots.domain.models.DeviceOwnerUser
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.findWinningLine
import by.klnvch.link5dots.domain.repositories.AddDotOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.UpdateStateOnlineRoomRepository

fun Point.isValidToBeAdded(board: Board, room: IRoom) =
    board.isInside(this) && room.isFree(this) && room.isNotOver()

fun IRoom.canMove(user: IUser) = (if (dots.size % 2 == 0) DeviceOwnerUser else BotUser) == user

interface AddDotUseCase {
    suspend fun addDot(p: Point)
}

class AddDotOnlineUseCase(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val board: Board,
    private val addDotRepository: AddDotOnlineRoomRepository,
    private val updateStateRepository: UpdateStateOnlineRoomRepository,
    private val getRepository: GetOnlineRoomRepository,
) : AddDotUseCase {
    override suspend fun addDot(p: Point) {
        getRepository.room?.let { room ->
            if (p.isValidToBeAdded(board, room)) {
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

class AddDotBotUseCase(
    private val timeService: TimeService,
    private val board: Board,
    private val getRepository: RoomGetRepository,
    private val saveRepository: RoomSaveRepository,
    private val bot: Bot,
) : AddDotUseCase {
    override suspend fun addDot(p: Point) {
        getRepository.get()?.let { room ->
            if (p.isValidToBeAdded(board, room) && room.canMove(DeviceOwnerUser)) {
                val dt = timeService.dt(room.time)
                var updatedRoom = room.move(DotImpl(p, Dot.HOST, dt))

                if (updatedRoom.isNotOver()) {
                    val botPoint = bot.findAnswer(updatedRoom.dots)
                    updatedRoom = updatedRoom.move(DotImpl(botPoint, Dot.GUEST, dt))
                }

                saveRepository.save(updatedRoom)
            }
        }
    }
}
