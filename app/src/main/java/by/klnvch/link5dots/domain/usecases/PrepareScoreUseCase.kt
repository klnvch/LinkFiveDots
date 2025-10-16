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

import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.GameResult
import by.klnvch.link5dots.domain.models.GameScore
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkGameScore
import by.klnvch.link5dots.domain.models.SimpleGameScore
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import javax.inject.Inject

interface PrepareScoreUseCase {
    fun get(room: IRoom): GameScore
}

class PrepareScoreBotUseCase @Inject constructor() : PrepareScoreUseCase {
    override fun get(room: IRoom) = BotGameScore(
        room.dots.size,
        room.getDuration(),
        room.getEndTime().toLong(),
        if (room.dots.size % 2 == 1) GameResult.WON else GameResult.LOST
    )
}

class PrepareScoreMultiplayerUseCase @Inject constructor(
    private val networkUserProvider: NetworkUserProvider,
) : PrepareScoreUseCase {
    override fun get(room: IRoom): NetworkGameScore {
        val user = networkUserProvider.networkUserOrThrow
        val status = if (room.user1 == user) {
            if (room.dots.size % 2 == 1) GameResult.WON
            else GameResult.LOST
        } else if (room.user2 == user) {
            if (room.dots.size % 2 == 0) GameResult.WON
            else GameResult.LOST
        } else throw IllegalStateException("User not found")

        return NetworkGameScore(
            room.dots.size,
            room.getDuration(),
            room.getEndTime().toLong(),
            status
        )
    }
}

class PrepareScoreOtherUseCase @Inject constructor() : PrepareScoreUseCase {
    override fun get(room: IRoom) = SimpleGameScore(
        room.dots.size,
        room.getDuration(),
        room.getEndTime().toLong(),
    )
}
