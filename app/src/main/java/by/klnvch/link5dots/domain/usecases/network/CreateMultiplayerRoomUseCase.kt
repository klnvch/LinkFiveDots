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

import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomKeyGenerator
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.TimeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

abstract class CreateMultiplayerRoomUseCase(
    private val settings: Settings,
    private val timeRepository: TimeRepository,
    private val roomKeyGenerator: RoomKeyGenerator,
) {
    suspend fun create(): RemoteRoomDescriptor {
        val userName = settings.getUserName().first()
        val timestamp = timeRepository.getCurrentTime()
        val key = roomKeyGenerator.get()
        val room = NetworkRoom(
            key,
            timestamp,
            emptyList(),
            getUser1(userName),
            null,
            type,
            RoomState.CREATED
        )
        return create(room)
    }

    abstract suspend fun create(room: NetworkRoom): RemoteRoomDescriptor
    abstract suspend fun getUser1(userName: String): NetworkUser
    abstract val type: Int
}

class CreateOnlineRoomUseCase @Inject constructor(
    private val repository: OnlineRoomRepository,
    private val firebaseManager: FirebaseManager,
    settings: Settings,
    timeRepository: TimeRepository,
    roomKeyGenerator: RoomKeyGenerator,
) : CreateMultiplayerRoomUseCase(settings, timeRepository, roomKeyGenerator) {
    override suspend fun create(room: NetworkRoom) =
        if (repository.isConnected()) repository.create(room)
        else throw DisconnectedError()

    override suspend fun getUser1(userName: String) =
        NetworkUser(firebaseManager.getUserId(), userName)

    override val type = RoomType.ONLINE
}

class CreateNsdRoomUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
    private val settings: Settings,
    timeRepository: TimeRepository,
    roomKeyGenerator: RoomKeyGenerator,
) : CreateMultiplayerRoomUseCase(settings, timeRepository, roomKeyGenerator) {
    override suspend fun create(room: NetworkRoom) = repository.create(room)
    override suspend fun getUser1(userName: String): NetworkUser {
        val userId = settings.getUserId().first()
        return NetworkUser(userId, userName)
    }

    override val type = RoomType.NSD

}

class DisconnectedError : Error()
