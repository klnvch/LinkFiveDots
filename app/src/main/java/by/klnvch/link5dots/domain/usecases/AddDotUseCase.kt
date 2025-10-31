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
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.toDot
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.SocketSendRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import javax.inject.Inject

class AddDotInfoUseCase @Inject constructor() : AddDotUseCase {
    override suspend fun addDot(p: Point) = Unit
}

class AddDotSocketUseCase @Inject constructor(
    getRepository: RoomRemoteRepository,
    board: Board,
    private val timeService: TimeService,
    private val sendRepository: SocketSendRepository,
    private val networkUserProvider: NetworkUserProvider,
) : AddDotCommonUseCase<INetworkRoom>(getRepository, board) {
    override val user: IUser get() = networkUserProvider.networkUserOrThrow
    override suspend fun addDot(room: INetworkRoom, p: Point) {
        val dt = timeService.dt(room.time)
        val updatedRoom = room.move(p.toDot(dt))
        sendRepository.send(updatedRoom)
    }
}

class AddDotTwoUseCase @Inject constructor(
    getRepository: RoomGetRepository,
    board: Board,
    private val timeService: TimeService,
    private val saveRepository: RoomRepository,
) : AddDotCommonUseCase<IRoom>(getRepository, board) {
    override val user: IUser? get() = null
    override suspend fun addDot(room: IRoom, p: Point) {
        val dt = timeService.dt(room.time)
        val updatedRoom = room.move(p.toDot(dt))
        saveRepository.save(updatedRoom, RoomType.TWO_PLAYERS)
    }
}
