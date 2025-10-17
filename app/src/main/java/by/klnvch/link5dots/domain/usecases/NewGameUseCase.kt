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

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class NewGameEmptyUseCase @Inject constructor() : NewGameUseCase {
    override val isImplemented = false
    override suspend fun create(seed: Long?) = Unit
}

class NewGameTwoUseCase @Inject constructor(
    roomKeyGenerator: RoomKeyGenerator,
    timeRepository: TimeService,
    private val roomRepository: RoomRepository,
) : NewGameOfflineUseCase(
    roomKeyGenerator,
    timeRepository,
) {
    override val user1 = null
    override val user2 = null
    override suspend fun save(room: IRoom) = roomRepository.save(room, RoomType.TWO_PLAYERS)
}

class NewGameBluetoothUseCase @Inject constructor(
    private val timeRepository: TimeService,
    private val roomKeyGenerator: RoomKeyGenerator,
    private val repository: BluetoothRoomRepository,
    private val networkUserProvider: NetworkUserProvider,
) : NewGameCommonUseCase() {
    override suspend fun create(seed: Long?) {
        if (repository.isServer()) {
            val prevRoom = repository.getFlow().firstOrNull()

            val key = roomKeyGenerator.generate()
            val time = timeRepository.time()

            val user1 = networkUserProvider.networkUserOrThrow
            val user2 = prevRoom?.user2

            val newRoom = NetworkRoom(
                key,
                time,
                getDots(seed),
                user1,
                user2,
                RoomState.CREATED
            )

            repository.send(newRoom)
        }
    }
}

class NewGameNsdUseCase @Inject constructor(
    private val timeRepository: TimeService,
    private val roomKeyGenerator: RoomKeyGenerator,
    private val repository: NsdRoomRepository,
    private val networkUserProvider: NetworkUserProvider,
) : NewGameCommonUseCase() {
    override suspend fun create(seed: Long?) {
        if (repository.isServer()) {
            val prevRoom = repository.getFlow().firstOrNull()

            val key = roomKeyGenerator.generate()
            val time = timeRepository.time()

            val user1 = networkUserProvider.networkUserOrThrow
            val user2 = prevRoom?.user2

            val newRoom = NetworkRoom(
                key,
                time,
                getDots(seed),
                user1,
                user2,
                RoomState.CREATED,
            )

            repository.send(newRoom)
        }
    }
}
