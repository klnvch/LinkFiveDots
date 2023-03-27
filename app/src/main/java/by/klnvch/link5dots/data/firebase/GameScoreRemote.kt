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

package by.klnvch.link5dots.data.firebase

import by.klnvch.link5dots.domain.models.GameResult
import by.klnvch.link5dots.domain.models.GameScore

data class GameScoreRemote(
    var userName: String? = null,
    var userId: String? = null,
    var androidId: String? = null,
    var score: Long = 0,
    var timestamp: Long? = null,
) {
    constructor(gameScore: GameScore, userId: String, androidId: String) : this(
        gameScore.userName,
        userId,
        androidId,
        gameScore.calcScore(),
        gameScore.timestamp
    )

    fun getDuration() = score % L_1000000

    fun getStatus(): GameResult {
        val temp = score / L_1000000
        return if (temp > L_2000) {
            GameResult.LOST
        } else {
            GameResult.WON
        }
    }

    fun getSize(): Int {
        val temp = score / L_1000000
        return if (temp > L_2000) {
            (L_4294 - temp).toInt()
        } else {
            temp.toInt()
        }
    }

    companion object {
        private const val L_1 = 1L
        private const val L_2000 = 2000L
        private const val L_4294 = 4294L
        private const val L_967295 = 967295L
        private const val L_1000000 = 1000000L

        private fun GameScore.calcScore(): Long {
            val tempTime = if (duration < L_1) L_1
            else if (duration > L_967295) L_967295
            else duration

            val tempMoves = if (size < L_1) L_1
            else if (size > L_2000) L_2000
            else size.toLong()

            return if (status == GameResult.WON) {
                tempMoves * L_1000000 + tempTime
            } else {
                (L_4294 - tempMoves) * L_1000000 + tempTime
            }
        }
    }
}
