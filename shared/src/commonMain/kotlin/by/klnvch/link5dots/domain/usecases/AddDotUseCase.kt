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
import by.klnvch.link5dots.domain.models.DeviceOwnerUser
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.bot.Bot
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.findWinningLine
import by.klnvch.link5dots.domain.repositories.AddDotOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.UpdateStateOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow

fun Point.isValidToBeAdded(board: Board, room: IRoom) =
    board.isInside(this) && room.isFree(this) && room.isNotOver()

interface AddDotUseCase {
    suspend fun addDot(p: Point)
}

class AddDotOnlineUseCase(
    private val networkUserProvider: NetworkUserProvider,
    private val board: Board,
    private val addDotRepository: AddDotOnlineRoomRepository,
    private val updateStateRepository: UpdateStateOnlineRoomRepository,
    private val getRepository: GetOnlineRoomRepository,
) : AddDotUseCase {
    override suspend fun addDot(p: Point) {
        getRepository.room?.let { room ->
            if (p.isValidToBeAdded(board, room)) {
                val user = networkUserProvider.networkUserOrThrow
                if (room.canMove(user)) {
                    val dot = DotImpl(p, 0)
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
        getRepository.room?.let { room ->
            if (p.isValidToBeAdded(board, room) && room.canMove(DeviceOwnerUser)) {
                val dt = timeService.dt(room.time)
                var updatedRoom = room.move(DotImpl(p, dt))

                if (updatedRoom.isNotOver()) {
                    val botPoint = bot.findAnswer(updatedRoom.dots.map { Point(it.x, it.y) })
                    updatedRoom = updatedRoom.move(DotImpl(botPoint, dt))
                }

                saveRepository.save(updatedRoom, RoomType.BOT)
            }
        }
    }
}
