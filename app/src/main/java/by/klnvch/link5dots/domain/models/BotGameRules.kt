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

package by.klnvch.link5dots.domain.models

import by.klnvch.link5dots.domain.models.RoomExt.getDuration
import by.klnvch.link5dots.models.Dot
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.utils.RoomUtils
import javax.inject.Inject

class BotGameRules @Inject constructor() : GameRules() {
    override val type = RoomType.BOT

    override fun init(room: Room?): Room {
        room?.user1 = DeviceOwnerUser()
        room?.user2 = BotUser()
        this.room = room ?: RoomUtils.createBotGame(DeviceOwnerUser(), BotUser())
        return this.room
    }

    override fun addDot(dot: Dot): Room {
        return RoomUtils.addDotWithBotAnswer(room, dot)
    }

    override fun undo(): Room {
        return RoomUtils.undo(RoomUtils.undo(room))
    }

    override fun getScore(): SimpleGameScore {
        return BotGameScore(
            room.dots.size,
            room.getDuration(),
            room.dots.last().timestamp,
            if (room.dots.last().type == Dot.HOST) GameResult.WON else GameResult.LOST
        )
    }
}
