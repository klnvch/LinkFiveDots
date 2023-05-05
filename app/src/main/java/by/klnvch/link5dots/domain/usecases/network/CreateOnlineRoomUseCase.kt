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
import by.klnvch.link5dots.domain.models.RoomKeyGenerator
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.TimeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CreateOnlineRoomUseCase @Inject constructor(
    private val firebaseManager: FirebaseManager,
    private val settings: Settings,
    private val onlineRoomRepository: OnlineRoomRepository,
    private val timeRepository: TimeRepository,
    private val roomKeyGenerator: RoomKeyGenerator,
) {
    suspend fun create(): NetworkRoom {
        val userId = firebaseManager.getUserId() ?: throw UnauthorizedError()
        val userName = settings.getUserName().first()
        val user1 = NetworkUser(userId, userName)
        val timestamp = timeRepository.getCurrentTime()
        val key = roomKeyGenerator.get()

        val room = NetworkRoom(
            key,
            timestamp,
            emptyList(),
            user1,
            null,
            RoomType.ONLINE,
            RoomState.CREATED
        )
        if (onlineRoomRepository.isConnected()) {
            onlineRoomRepository.create(room)
            return room
        } else {
            throw DisconnectedError()
        }
    }
}

class UnauthorizedError : Error()
class DisconnectedError : Error()
