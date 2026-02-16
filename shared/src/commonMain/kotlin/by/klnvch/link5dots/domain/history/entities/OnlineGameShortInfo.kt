package by.klnvch.link5dots.domain.history.entities

import by.klnvch.link5dots.domain.formatDuration
import by.klnvch.link5dots.domain.history.entities.impl.OnlineGameShortInfoImpl
import by.klnvch.link5dots.domain.history.entities.impl.OnlineHistoryStatsImpl
import by.klnvch.link5dots.domain.history.entities.impl.OpponentImpl
import by.klnvch.link5dots.domain.history.entities.impl.PerformanceImpl
import by.klnvch.link5dots.formatDateTime
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class OnlineGameShortInfoStatus { Won, Lost, Draw, InProgress }

@OptIn(ExperimentalJsExport::class)
@JsExport
interface OnlineGameShortInfo {
    val opponent: Opponent      // opponent with real name or default
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
interface Opponent {
    val id: String
    val name: String
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface OnlineHistoryStats {
    val totalPerformance: Performance
    val performanceByUser: Map<Opponent, Performance>
    val items: List<OnlineGameShortInfo>
}

fun calculateStats(items: List<OnlineGameShortInfo>): OnlineHistoryStats {
    val totalPerformance = PerformanceImpl()
    val performanceByUser = mutableMapOf<Opponent, PerformanceImpl>()

    for (item in items.reversed()) {
        val perfByUser = performanceByUser[item.opponent] ?: PerformanceImpl()

        when (item.status) {
            OnlineGameShortInfoStatus.Won -> {
                totalPerformance.wins++
                perfByUser.wins++
            }

            OnlineGameShortInfoStatus.Lost -> {
                totalPerformance.losses++
                perfByUser.losses++
            }

            OnlineGameShortInfoStatus.Draw -> {
                totalPerformance.draws++
                perfByUser.draws++
            }

            OnlineGameShortInfoStatus.InProgress -> {}
        }

        performanceByUser[item.opponent] = perfByUser
    }

    return OnlineHistoryStatsImpl(items, totalPerformance, performanceByUser)
}

private val GameStatus.toOnlineGameShortInfoStatus
    get(): OnlineGameShortInfoStatus = when (this) {
        GameStatus.Won, GameStatus.WonByTimeout -> OnlineGameShortInfoStatus.Won
        GameStatus.Lost, GameStatus.LostByTimeout -> OnlineGameShortInfoStatus.Lost
        GameStatus.Draw -> OnlineGameShortInfoStatus.Draw
    }

fun HistoryOnlineRoomItem.toOnlineGameShortInfo(defaultName: String): OnlineGameShortInfo =
    OnlineGameShortInfoImpl(
        opponent = OpponentImpl(userId, userName ?: defaultName),
        timeText = time.formatDateTime(),
        sizeText = result?.size?.toString() ?: "…",
        durationText = result?.duration?.formatDuration() ?: "…",
        status = result?.status?.toOnlineGameShortInfoStatus ?: OnlineGameShortInfoStatus.InProgress
    )
