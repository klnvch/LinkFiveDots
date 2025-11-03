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
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.bot.Bot
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.findWinningLine
import by.klnvch.link5dots.domain.models.toDot
import by.klnvch.link5dots.domain.repositories.AddDotOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.GetRoomRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.UpdateStateOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow

/**
 * Must be inside board, be free and game is not over
 */
fun Point.isValidToBeAdded(board: Board, room: IRoom) =
    board.isInside(this) && room.isFree(this) && room.isNotOver()

interface AddDotUseCase {
    suspend fun addDot(p: Point)
}

abstract class AddDotCommonUseCase<Room : IRoom>(
    private val getRepository: GetRoomRepository<Room>,
    private val board: Board,
) : AddDotUseCase {
    protected abstract val user: IUser?
    protected abstract suspend fun addDot(room: Room, p: Point)
    override suspend fun addDot(p: Point) {
        getRepository.room?.let {
            if (p.isValidToBeAdded(board, it)) {
                if (it.canMove(user)) {
                    addDot(it, p)
                }
            }
        }
    }
}

class AddDotOnlineUseCase(
    getRepository: RoomRemoteRepository,
    board: Board,
    private val networkUserProvider: NetworkUserProvider,
    private val addDotRepository: AddDotOnlineRoomRepository,
    private val updateStateRepository: UpdateStateOnlineRoomRepository,
) : AddDotCommonUseCase<INetworkRoom>(getRepository, board) {
    override val user: IUser get() = networkUserProvider.networkUserOrThrow
    override suspend fun addDot(room: INetworkRoom, p: Point) {
        addDotRepository.addDot(room.key, room.dots.size, p)
        if ((room.dots + p).findWinningLine() != null) {
            updateStateRepository.update(room.key, RoomState.FINISHED)
        }
    }
}

class AddDotBotUseCase(
    getRepository: RoomGetRepository,
    board: Board,
    private val timeService: TimeService,
    private val saveRepository: RoomSaveLocalRepository,
    private val bot: Bot,
) : AddDotCommonUseCase<IRoom>(getRepository, board) {
    override val user: IUser get() = DeviceOwnerUser
    override suspend fun addDot(room: IRoom, p: Point) {
        val dt = timeService.dt(room.time)
        var updatedRoom = room.move(p.toDot(dt))

        if (updatedRoom.isNotOver()) {
            val botPoint = bot.findAnswer(updatedRoom.dots)
            updatedRoom = updatedRoom.move(botPoint.toDot(dt))
        }

        saveRepository.save(updatedRoom)
    }
}
