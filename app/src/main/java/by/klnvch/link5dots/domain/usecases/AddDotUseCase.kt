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
package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.Bot
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkRoomExtended
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.TimeRepository
import javax.inject.Inject

interface AddDotUseCase {
    suspend fun addDot(room: IRoom, p: Point)
}

class AddDotInfoUseCase @Inject constructor() : AddDotUseCase {
    override suspend fun addDot(room: IRoom, p: Point) = Unit
}

abstract class AddDotRealUseCase constructor(
    private val timeRepository: TimeRepository,
    private val board: Board,
) : AddDotUseCase {
    override suspend fun addDot(room: IRoom, p: Point) {
        if (board.isInside(p) && room.isFree(p) && room.isNotOver()) {
            val dt = (timeRepository.getCurrentTime() - room.timestamp).toInt()
            addInternal(room, p, dt)
        }
    }

    abstract suspend fun addInternal(room: IRoom, p: Point, dt: Int)
}

abstract class AddDotMultiplayerUseCase constructor(
    timeRepository: TimeRepository,
    board: Board,
) : AddDotRealUseCase(timeRepository, board) {
    abstract suspend fun addMultiplayerDot(room: IRoom, dot: Dot)
    override suspend fun addInternal(room: IRoom, p: Point, dt: Int) {
        if (room is NetworkRoomExtended) {
            val (_, _, dots, user1, user2, _, _,  yourId) = room
            if (user1.id == yourId && dots.size % 2 == 0) {
                addMultiplayerDot(room, Dot(p, Dot.HOST, dt))
            } else if (user2?.id == yourId && dots.size % 2 == 1) {
                addMultiplayerDot(room, Dot(p, Dot.GUEST, dt))
            }
        } else {
            throw IllegalStateException("Wrong room type")
        }
    }
}

class AddDotOnlineUseCase @Inject constructor(
    timeRepository: TimeRepository,
    board: Board,
    private val repository: OnlineRoomRepository,
) : AddDotMultiplayerUseCase(timeRepository, board) {

    override suspend fun addMultiplayerDot(room: IRoom, dot: Dot) =
        repository.addDot(room.key, room.dots.size, dot)
}

class AddDotNsdUseCase @Inject constructor(
    timeRepository: TimeRepository,
    board: Board,
    private val repository: NsdRoomRepository,
) : AddDotMultiplayerUseCase(timeRepository, board) {
    override suspend fun addMultiplayerDot(room: IRoom, dot: Dot) = repository.addDot(dot)
}

abstract class AddDotOfflineUseCase(
    timeRepository: TimeRepository,
    board: Board,
    private val roomRepository: RoomRepository,
) : AddDotRealUseCase(timeRepository, board) {
    override suspend fun addInternal(room: IRoom, p: Point, dt: Int) {
        val updatedRoom = modify(Room(room), p, dt)
        roomRepository.save(updatedRoom)
    }

    abstract fun modify(room: Room, p: Point, dt: Int): Room
}

class AddDotBotUseCase @Inject constructor(
    timeRepository: TimeRepository,
    board: Board,
    roomRepository: RoomRepository,
    private val bot: Bot,
) : AddDotOfflineUseCase(timeRepository, board, roomRepository) {
    override fun modify(room: Room, p: Point, dt: Int): Room {
        if (room.dots.size % 2 == 0) {
            room.add(Dot(p, Dot.HOST, dt))
            if (room.isNotOver()) {
                val botDot = bot.findAnswer(room.dots)
                room.add(botDot.copy(type = Dot.GUEST))
            }
        }
        return room
    }
}

class AddDotTwoUseCase @Inject constructor(
    timeRepository: TimeRepository,
    board: Board,
    roomRepository: RoomRepository,
) : AddDotOfflineUseCase(timeRepository, board, roomRepository) {
    override fun modify(room: Room, p: Point, dt: Int): Room {
        val lastDotType = room.dots.lastOrNull()?.type ?: Dot.GUEST
        val type = if (lastDotType == Dot.GUEST) Dot.HOST else Dot.GUEST
        room.add(Dot(p, type, dt))
        return room
    }
}
