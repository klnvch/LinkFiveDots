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

import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.GameResult
import by.klnvch.link5dots.domain.models.GameScore
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkGameScore
import by.klnvch.link5dots.domain.models.NetworkRoomExtended
import by.klnvch.link5dots.domain.models.SimpleGameScore
import javax.inject.Inject

interface PrepareScoreUseCase {
    fun get(room: IRoom): GameScore
}

class PrepareScoreBotUseCase @Inject constructor() : PrepareScoreUseCase {
    override fun get(room: IRoom) = BotGameScore(
        room.dots.size,
        room.getDuration(),
        room.getEndTime(),
        if (room.dots.last().type == Dot.HOST) GameResult.WON else GameResult.LOST
    )
}

class PrepareScoreMultiplayerUseCase @Inject constructor(
) : PrepareScoreUseCase {
    override fun get(room: IRoom): NetworkGameScore {
        val status = if (room is NetworkRoomExtended) {
            if (room.user1.id == room.yourId) {
                if (room.dots.size % 2 == 1) GameResult.WON
                else GameResult.LOST
            } else if (room.user2?.id == room.yourId) {
                if (room.dots.size % 2 == 0) GameResult.WON
                else GameResult.LOST
            } else throw IllegalStateException("User not found")
        } else throw IllegalStateException("Wrong room type")

        return NetworkGameScore(
            room.dots.size,
            room.getDuration(),
            room.getEndTime(),
            status
        )
    }
}

class PrepareScoreOtherUseCase @Inject constructor() : PrepareScoreUseCase {
    override fun get(room: IRoom) = SimpleGameScore(
        room.dots.size,
        room.getDuration(),
        room.getEndTime(),
    )
}
