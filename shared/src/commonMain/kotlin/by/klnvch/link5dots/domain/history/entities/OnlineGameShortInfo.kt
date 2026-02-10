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

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Performance {
    val wins: Int
    val losses: Int
    val draws: Int
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface OnlineHistoryStats {
    val totalPerformance: Performance
    val items: List<OnlineGameShortInfo>
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

private data class PerformanceImpl(
    override val wins: Int,
    override val losses: Int,
    override val draws: Int,
) : Performance

private data class OnlineHistoryStatsImpl(
    override val items: List<OnlineGameShortInfo>,
    override val totalPerformance: PerformanceImpl,
) : OnlineHistoryStats

fun calculateStats(items: List<OnlineGameShortInfo>): OnlineHistoryStats {
    var wins = 0
    var losses = 0
    var draws = 0

    for (item in items) {
        when (item.status) {
            OnlineGameShortInfoStatus.Won -> wins++
            OnlineGameShortInfoStatus.Lost -> losses++
            OnlineGameShortInfoStatus.Draw -> draws++
            OnlineGameShortInfoStatus.InProgress -> {}
        }
    }

    return OnlineHistoryStatsImpl(items, PerformanceImpl(wins, losses, draws))
}

private val GameStatus.toOnlineGameShortInfoStatus
    get(): OnlineGameShortInfoStatus = when (this) {
        GameStatus.Won, GameStatus.WonByTimeout -> OnlineGameShortInfoStatus.Won
        GameStatus.Lost, GameStatus.LostByTimeout -> OnlineGameShortInfoStatus.Lost
        GameStatus.Draw -> OnlineGameShortInfoStatus.Draw
    }

fun HistoryOnlineRoomItem.toOnlineGameShortInfo(defaultName: String): OnlineGameShortInfo =
    OnlineGameShortInfoImpl(
        user1Name = this.user1Name ?: defaultName,
        user2Name = this.user2Name ?: defaultName,
        timeText = this.time.formatDateTime(),
        sizeText = result?.size?.toString() ?: "…",
        durationText = result?.duration?.formatDuration() ?: "…",
        status = result?.status?.toOnlineGameShortInfoStatus ?: OnlineGameShortInfoStatus.InProgress
    )
