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

interface GameResult {
    /** number of dots to measure complexity */
    val size: Int

    /** number of seconds since game started */
    val duration: Int

    /** all possible reasons to stop a game */
    val status: GameStatus
}

interface HistoryOnlineRoomItem {
    /** name or default if null */
    val user1Name: String?

    /** name or default if null */
    val user2Name: String?

    /** number of seconds since last epoch to measure complexity */
    val time: Int

    /** game result or null if in progress */
    val result: GameResult?
}

@Serializable
data class GameResultImpl(
    @SerialName("s") override val size: Int,
    @SerialName("d") override val duration: Int,
    @SerialName("o") override val status: GameStatus,
) : GameResult

@Serializable
data class HistoryOnlineRoomItemImpl(
    @SerialName("u1") override val user1Name: String?,
    @SerialName("u2") override val user2Name: String?,
    @SerialName("t") override val time: Int,
    @SerialName("r") override val result: GameResultImpl?,
) : HistoryOnlineRoomItem

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
        GameResultImpl(size, duration, status)
    } else null

    val value = HistoryOnlineRoomItemImpl(user1Name, user2Name, time, result)
    val bytes = Cbor.encodeToByteArray(HistoryOnlineRoomItemImpl.serializer(), value)
    return Base64.encode(bytes)
}

@OptIn(ExperimentalSerializationApi::class)
fun decode(value: String): HistoryOnlineRoomItem {
    val bytes = Base64.decode(value)
    return Cbor.decodeFromByteArray(HistoryOnlineRoomItemImpl.serializer(), bytes)
}
