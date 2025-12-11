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

package by.klnvch.link5dots.domain.models.online

import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.size
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlin.io.encoding.Base64

@Serializable
enum class GameStatus {
    @SerialName("w")
    Won,

    @SerialName("wt")
    WonByTimeout,

    @SerialName("l")
    Lost,

    @SerialName("lt")
    LostByTimeout,

    @SerialName("d")
    Draw,
}

@Serializable
data class GameResult(
    @SerialName("s") val size: Int,
    @SerialName("d") val duration: Int,
    @SerialName("o") val status: GameStatus,
)

@Serializable
data class HistoryOnlineRoomItem(
    @SerialName("u1") val user1Name: String?,
    @SerialName("u2") val user2Name: String?,
    @SerialName("t") val time: Int,
    @SerialName("r") val result: GameResult?,
)

@OptIn(ExperimentalSerializationApi::class)
fun encode(user: NetworkUser, room: OnlineRoomLive): String {
    val user1Name = room.room.user1.name
    val user2Name = room.room.user2.name
    val time = room.room.time
    val isOver = room.room.isOver()
    val isActive = room.isActive
    val canMove = room.room.canMove(user)
    val result = if (isOver || !isActive) {
        val size = room.room.size
        val duration = room.room.getDuration()
        val status = when {
            isOver && !canMove -> GameStatus.Won
            isOver && canMove -> GameStatus.Lost
            !isActive && !canMove -> GameStatus.WonByTimeout
            !isActive && canMove -> GameStatus.LostByTimeout
            else -> GameStatus.Draw
        }
        GameResult(size, duration, status)
    } else null

    val value = HistoryOnlineRoomItem(user1Name, user2Name, time, result)
    val bytes = Cbor.encodeToByteArray(HistoryOnlineRoomItem.serializer(), value)
    return Base64.encode(bytes)
}
