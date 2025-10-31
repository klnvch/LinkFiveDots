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

package by.klnvch.link5dots.domain.models

import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.TimeService

fun generateDots() = generateInitialGame(gameSeed())
    .map { it.toDot(0) }
    .toMutableList()

interface RoomFactory {
    fun generate(): IRoom
}

abstract class RoomCommonFactory(
    private val roomKeyGenerator: RoomKeyGenerator,
    private val timeService: TimeService,
) : RoomFactory {
    protected abstract val user1: IUser?
    protected abstract val user2: IUser?
    override fun generate(): IRoom = Room(
        roomKeyGenerator.generate(),
        timeService.time(),
        generateDots(),
        user1,
        user2,
    )
}

class RoomBotFactory(
    roomKeyGenerator: RoomKeyGenerator,
    timeService: TimeService,
) : RoomCommonFactory(roomKeyGenerator, timeService) {
    override val user1 = DeviceOwnerUser
    override val user2 = BotUser
}
