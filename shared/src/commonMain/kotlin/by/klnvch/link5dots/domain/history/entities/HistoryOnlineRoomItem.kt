package by.klnvch.link5dots.domain.history.entities

import by.klnvch.link5dots.domain.history.entities.impl.GameResultImpl
import by.klnvch.link5dots.domain.history.entities.impl.HistoryOnlineRoomItemImpl
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.getDuration
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
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

    /** number of seconds spent on thinking */
    val dt: Int

    /** all possible reasons to stop a game */
    val status: GameStatus
}

interface HistoryOnlineRoomItem {
    /** opponent user id */
    val userId: String

    /** opponent user name or default if null */
    val userName: String?

    /** number of seconds since last epoch to measure complexity */
    val time: Int

    /** game result or null if in progress */
    val result: GameResult?
}

fun OnlineRoomLive.mapToHistoryOnlineRoomItem(user: NetworkUser): HistoryOnlineRoomItem {
    val userId = if (room.user1.id === user.id) room.user2.id else room.user1.id
    val userName = if (room.user1.id === user.id) room.user2.name else room.user1.name
    val time = room.time
    val isOver = room.isOver()
    val canMove = room.canMove(user)
    val result = if (isOver || !isActive) {
        val size = room.size
        val duration = room.getDuration()
        val dt = room.dots.getDuration(if (room.user1.id === user.id) 1 else 0)
        val status = when {
            isOver && !canMove -> GameStatus.Won
            isOver && canMove -> GameStatus.Lost
            !isActive && !canMove -> GameStatus.WonByTimeout
            !isActive && canMove -> GameStatus.LostByTimeout
            else -> GameStatus.Draw
        }
        GameResultImpl(size, duration, dt, status)
    } else null

    return HistoryOnlineRoomItemImpl(userId, userName, time, result)
}

@OptIn(ExperimentalSerializationApi::class)
fun HistoryOnlineRoomItem.encode(): String {
    val value = this as HistoryOnlineRoomItemImpl
    val bytes = Cbor.encodeToByteArray(HistoryOnlineRoomItemImpl.serializer(), value)
    return Base64.encode(bytes)
}

@OptIn(ExperimentalSerializationApi::class)
fun String.decode(): HistoryOnlineRoomItem {
    val bytes = Base64.decode(this)
    return Cbor.decodeFromByteArray(HistoryOnlineRoomItemImpl.serializer(), bytes)
}
