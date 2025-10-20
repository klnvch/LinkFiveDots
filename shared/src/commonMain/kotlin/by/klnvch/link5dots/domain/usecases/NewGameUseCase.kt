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

import by.klnvch.link5dots.domain.models.BotUser
import by.klnvch.link5dots.domain.models.DeviceOwnerUser
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.generateInitialGame
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.RoomSaveRepository
import by.klnvch.link5dots.domain.repositories.TimeService

interface NewGameUseCase {
    val isImplemented: Boolean
    suspend fun create(seed: Long?)
}

abstract class NewGameCommonUseCase() : NewGameUseCase {
    override val isImplemented = true
    protected fun getDots(seed: Long?) =
        if (seed != null) generateInitialGame(seed)
            .map { DotImpl(it, 0) }
            .toMutableList<Dot>()
        else mutableListOf()
}

abstract class NewGameOfflineUseCase(
    private val roomKeyGenerator: RoomKeyGenerator,
    private val timeRepository: TimeService,
) : NewGameCommonUseCase() {
    override suspend fun create(seed: Long?) {
        val room = Room(
            roomKeyGenerator.generate(),
            timeRepository.time(),
            getDots(seed),
            user1,
            user2,
        )
        save(room)
    }

    protected abstract val user1: IUser?
    protected abstract val user2: IUser?
    protected abstract suspend fun save(room: IRoom)
}

class NewGameBotUseCase(
    roomKeyGenerator: RoomKeyGenerator,
    timeRepository: TimeService,
    private val roomRepository: RoomSaveRepository,
) : NewGameOfflineUseCase(
    roomKeyGenerator,
    timeRepository,
) {
    override val user1 = DeviceOwnerUser
    override val user2 = BotUser
    override suspend fun save(room: IRoom) = roomRepository.save(room, RoomType.BOT)
}

class NewGameOnlineUseCase() : NewGameUseCase {
    override val isImplemented = false
    override suspend fun create(seed: Long?) = Unit
}
