package by.klnvch.link5dots.domain.history.entities

import by.klnvch.link5dots.domain.formatDuration
import by.klnvch.link5dots.formatDateTime
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

////////////////////////////////////////////////////////////////////////////////////////////////////
// Interfaces
////////////////////////////////////////////////////////////////////////////////////////////////////
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class OnlineGameShortInfoStatus { Won, Lost, Draw, InProgress }

@OptIn(ExperimentalJsExport::class)
@JsExport
interface OnlineGameShortInfo {
    val user1Name: String          // real name or default
    val user2Name: String          // real name or default
    val timeText: String           // e.g. "02:15"
    val sizeText: String           // e.g. "12"
    val durationText: String       // e.g. "Dec 18, 05:22 PM"
    val status: OnlineGameShortInfoStatus
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Implementation
////////////////////////////////////////////////////////////////////////////////////////////////////
data class OnlineGameShortInfoImpl(
    override val user1Name: String,          // real name or default
    override val user2Name: String,          // real name or default
    override val timeText: String,           // e.g. "02:15"
    override val sizeText: String,           // e.g. "12"
    override val durationText: String,       // e.g. "Dec 18, 05:22 PM"
    override val status: OnlineGameShortInfoStatus,
) : OnlineGameShortInfo

fun HistoryOnlineRoomItem.toOnlineGameShortInfo(defaultName: String): OnlineGameShortInfo =
    OnlineGameShortInfoImpl(
        user1Name = this.user1Name ?: defaultName,
        user2Name = this.user2Name ?: defaultName,
        timeText = this.time.formatDateTime(),
        sizeText = result?.size?.toString() ?: "…",
        durationText = result?.duration?.formatDuration() ?: "…",
        status = when (result?.status) {
            GameStatus.Won -> OnlineGameShortInfoStatus.Won
            GameStatus.WonByTimeout -> OnlineGameShortInfoStatus.Won
            GameStatus.Lost -> OnlineGameShortInfoStatus.Lost
            GameStatus.LostByTimeout -> OnlineGameShortInfoStatus.Lost
            GameStatus.Draw -> OnlineGameShortInfoStatus.Draw
            null -> OnlineGameShortInfoStatus.InProgress
        }
    )
