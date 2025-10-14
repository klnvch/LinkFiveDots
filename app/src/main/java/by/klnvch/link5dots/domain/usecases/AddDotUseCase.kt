/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.UnauthorizedException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddDotInfoUseCase @Inject constructor() : AddDotUseCase {
    override suspend fun addDot(p: Point) = Unit
}

abstract class AddDotRealUseCase(
    private val timeService: TimeService,
    private val board: Board,
    private val getRepository: RoomGetRepository,
) : AddDotUseCase {
    fun dt(time: Int) = timeService.dt(time)
    override suspend fun addDot(p: Point) {
        getRepository.room?.let { room ->
            if (p.isValidToBeAdded(board, room)) {
                val dt = dt(room.time)
                addInternal(room, p, dt)
            }
        }
    }

    abstract suspend fun addInternal(room: IRoom, p: Point, dt: Int)
}

abstract class AddDotMultiplayerUseCase(
    timeService: TimeService,
    board: Board,
    getRepository: RoomGetRepository,
    private val networkUserProvider: NetworkUserProvider,
) : AddDotRealUseCase(timeService, board, getRepository) {
    abstract suspend fun addMultiplayerDot(room: IRoom, dot: Dot)
    override suspend fun addInternal(room: IRoom, p: Point, dt: Int) {
        val user = networkUserProvider.networkUser ?: throw UnauthorizedException()
        if (room.canMove(user)) {
            addMultiplayerDot(room, DotImpl(p, dt))
        }
    }
}

class AddDotNsdUseCase @Inject constructor(
    timeService: TimeService,
    board: Board,
    private val repository: NsdRoomRepository,
    networkUserProvider: NetworkUserProvider,
) : AddDotMultiplayerUseCase(timeService, board, repository, networkUserProvider) {
    override suspend fun addMultiplayerDot(room: IRoom, dot: Dot) {
        val currentRoom = repository.getFlow().filterNotNull().first()
        val updatedRoom = currentRoom.move(dot)
        repository.update(updatedRoom)
    }
}

class AddDotBluetoothUseCase @Inject constructor(
    timeService: TimeService,
    board: Board,
    private val repository: BluetoothRoomRepository,
    networkUserProvider: NetworkUserProvider,
) : AddDotMultiplayerUseCase(timeService, board, repository, networkUserProvider) {
    override suspend fun addMultiplayerDot(room: IRoom, dot: Dot) {
        val currentRoom = repository.getFlow().filterNotNull().first()
        val updatedRoom = currentRoom.move(dot)
        repository.update(updatedRoom)
    }
}

class AddDotTwoUseCase @Inject constructor(
    timeRepository: TimeService,
    board: Board,
    getRepository: RoomGetRepository,
    private val saveRepository: RoomRepository,
) : AddDotRealUseCase(timeRepository, board, getRepository) {
    override suspend fun addInternal(room: IRoom, p: Point, dt: Int) {
        val updatedRoom = room.move(DotImpl(p, dt))
        saveRepository.save(updatedRoom, RoomType.TWO_PLAYERS)
    }
}
