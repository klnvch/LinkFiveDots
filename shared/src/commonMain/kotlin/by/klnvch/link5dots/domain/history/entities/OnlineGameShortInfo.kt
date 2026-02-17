package by.klnvch.link5dots.domain.history.entities

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
    val timeText: String        // e.g. "02:15"
    val size: Int
    val duration: Int
    val status: OnlineGameShortInfoStatus
}

@OptIn(ExperimentalJsExport::class)
@JsExport
interface Performance {
    val wins: Int
    val losses: Int
    val draws: Int
    val total: Int
    val duration: Int
    val durationAvg: Int
    val size: Int
    val sizeAvg: Int
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
                totalPerformance.duration += item.duration
                totalPerformance.size += item.size
                perfByUser.wins++
                perfByUser.duration += item.duration
                perfByUser.size += item.size
            }

            OnlineGameShortInfoStatus.Lost -> {
                totalPerformance.losses++
                totalPerformance.duration += item.duration
                totalPerformance.size += item.size
                perfByUser.losses++
                perfByUser.duration += item.duration
                perfByUser.size += item.size
            }

            OnlineGameShortInfoStatus.Draw -> {
                totalPerformance.draws++
                totalPerformance.duration += item.duration
                totalPerformance.size += item.size
                perfByUser.draws++
                perfByUser.duration += item.duration
                perfByUser.size += item.size
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
        size = result?.size ?: 0,
        duration = result?.duration ?: 0,
        status = result?.status?.toOnlineGameShortInfoStatus ?: OnlineGameShortInfoStatus.InProgress
    )
