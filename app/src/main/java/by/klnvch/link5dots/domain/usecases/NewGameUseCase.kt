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

import by.klnvch.link5dots.domain.models.ActionAvailability
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.TimeService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class NewGameEmptyUseCase @Inject constructor() : NewGameUseCase {
    override val isImplemented = false
    override suspend fun create(seed: Long?) = Unit
    override val actionAvailability = ActionAvailability.Gone
}

class NewGameTwoUseCase @Inject constructor(
    roomKeyGenerator: RoomKeyGenerator,
    timeRepository: TimeService,
    roomRepository: RoomRepository,
) : NewGameOfflineUseCase(
    roomKeyGenerator,
    timeRepository,
    roomRepository,
) {
    override val user1 = null
    override val user2 = null
    override val type = RoomType.TWO_PLAYERS
}

class NewGameBluetoothUseCase @Inject constructor(
    private val settings: Settings,
    private val timeRepository: TimeService,
    private val roomKeyGenerator: RoomKeyGenerator,
    private val repository: BluetoothRoomRepository,
) : NewGameCommonUseCase() {
    override suspend fun create(seed: Long?) {
        if (repository.isServer()) {
            val prevRoom = repository.getFlow().firstOrNull()

            val key = roomKeyGenerator.generate()
            val time = timeRepository.time()

            val userName = settings.getUserName()
            val userId = settings.getUserId().first()
            val user1 = NetworkUser(userId, userName)
            val user2 = prevRoom?.user2

            val newRoom = NetworkRoom(
                key,
                time,
                getDots(seed),
                user1,
                user2,
                RoomType.BLUETOOTH,
                RoomState.CREATED
            )

            repository.update(newRoom)
        }
    }

    override val actionAvailability
        get() = if (repository.isServer()) ActionAvailability.Available else ActionAvailability.Gone
}

class NewGameNsdUseCase @Inject constructor(
    private val settings: Settings,
    private val timeRepository: TimeService,
    private val roomKeyGenerator: RoomKeyGenerator,
    private val repository: NsdRoomRepository,
) : NewGameCommonUseCase() {
    override suspend fun create(seed: Long?) {
        if (repository.isServer()) {
            val prevRoom = repository.getFlow().firstOrNull()

            val key = roomKeyGenerator.generate()
            val time = timeRepository.time()

            val userName = settings.getUserName()
            val userId = settings.getUserId().first()
            val user1 = NetworkUser(userId, userName)
            val user2 = prevRoom?.user2

            val newRoom = NetworkRoom(
                key,
                time,
                getDots(seed),
                user1,
                user2,
                RoomType.NSD,
                RoomState.CREATED,
            )

            repository.update(newRoom)
        }
    }

    override val actionAvailability
        get() = if (repository.isServer()) ActionAvailability.Available else ActionAvailability.Gone
}
