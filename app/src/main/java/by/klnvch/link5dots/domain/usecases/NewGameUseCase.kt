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

import by.klnvch.link5dots.domain.models.BotUser
import by.klnvch.link5dots.domain.models.DeviceOwnerUser
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.InitialGameGenerator
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.models.RoomKeyGenerator
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.translate
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.TimeRepository
import java.lang.IllegalStateException
import javax.inject.Inject

interface NewGameUseCase {
    suspend fun create(seed: Long?)
    val isSupported: Boolean
}

class NewGameEmptyUseCase @Inject constructor() : NewGameUseCase {
    override suspend fun create(seed: Long?) = throw IllegalStateException()
    override val isSupported = false
}

abstract class NewGameOfflineUseCase(
    private val roomKeyGenerator: RoomKeyGenerator,
    private val timeRepository: TimeRepository,
    private val initialGameGenerator: InitialGameGenerator,
    private val roomRepository: RoomRepository,
) : NewGameUseCase {
    override val isSupported = true
    override suspend fun create(seed: Long?) {
        val room = Room(
            roomKeyGenerator.get(),
            timeRepository.getCurrentTime(),
            mutableListOf(),
            user1,
            user2,
            type,
        )
        if (seed != null) {
            val tp = Point(8, 8)
            initialGameGenerator.get(seed)
                .map { it.translate(tp) }
                .withIndex()
                .forEach { (i, p) ->
                    val type = if (i % 2 == 0) Dot.HOST else Dot.GUEST
                    room.add(Dot(p, type, 0))
                }
        }
        roomRepository.save(room)
    }

    abstract val user1: IUser?
    abstract val user2: IUser?
    abstract val type: Int
}

class NewGameBotUseCase @Inject constructor(
    roomKeyGenerator: RoomKeyGenerator,
    timeRepository: TimeRepository,
    initialGameGenerator: InitialGameGenerator,
    roomRepository: RoomRepository,
) : NewGameOfflineUseCase(
    roomKeyGenerator,
    timeRepository, initialGameGenerator,
    roomRepository,
) {
    override val user1 = DeviceOwnerUser
    override val user2 = BotUser
    override val type = RoomType.BOT

}

class NewGameTwoUseCase @Inject constructor(
    roomKeyGenerator: RoomKeyGenerator,
    timeRepository: TimeRepository,
    initialGameGenerator: InitialGameGenerator,
    roomRepository: RoomRepository,
) : NewGameOfflineUseCase(
    roomKeyGenerator,
    timeRepository, initialGameGenerator,
    roomRepository,
) {
    override val user1 = null
    override val user2 = null
    override val type = RoomType.TWO_PLAYERS
}
