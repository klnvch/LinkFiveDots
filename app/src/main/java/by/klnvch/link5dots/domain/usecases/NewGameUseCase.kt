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

import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.generateDots
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.SocketGetRepository
import by.klnvch.link5dots.domain.repositories.SocketSendRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import javax.inject.Inject

class NewGameSocketUseCase @Inject constructor(
    private val timeRepository: TimeService,
    private val roomKeyGenerator: RoomKeyGenerator,
    private val networkUserProvider: NetworkUserProvider,
    private val getRepository: SocketGetRepository,
    private val sendRepository: SocketSendRepository,
) : NewGameUseCase {
    override val isImplemented = true
    override suspend fun create() {
        val user1 = networkUserProvider.networkUserOrThrow
        val room = getRepository.room
        if (room?.user1 == user1) {
            val newRoom = NetworkRoom(
                roomKeyGenerator.generate(),
                timeRepository.time(),
                generateDots(),
                user1,
                getRepository.room?.user2,
                RoomState.CREATED
            )
            sendRepository.send(newRoom)
        }
    }
}
