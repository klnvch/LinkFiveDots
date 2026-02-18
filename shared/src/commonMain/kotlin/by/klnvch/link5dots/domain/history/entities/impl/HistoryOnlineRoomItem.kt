package by.klnvch.link5dots.domain.history.entities.impl

import by.klnvch.link5dots.domain.history.entities.GameResult
import by.klnvch.link5dots.domain.history.entities.GameStatus
import by.klnvch.link5dots.domain.history.entities.HistoryOnlineRoomItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GameResultImpl(
    @SerialName("s") override val size: Int,
    @SerialName("d") override val duration: Int,
    @SerialName("t") override val dt: Int,
    @SerialName("o") override val status: GameStatus,
) : GameResult

@Serializable
internal data class HistoryOnlineRoomItemImpl(
    @SerialName("uI") override val userId: String,
    @SerialName("uN") override val userName: String?,
    @SerialName("t") override val time: Int,
    @SerialName("r") override val result: GameResultImpl?,
) : HistoryOnlineRoomItem
