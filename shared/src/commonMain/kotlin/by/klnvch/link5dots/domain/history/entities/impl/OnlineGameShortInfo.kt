package by.klnvch.link5dots.domain.history.entities.impl

import by.klnvch.link5dots.domain.history.entities.OnlineGameShortInfo
import by.klnvch.link5dots.domain.history.entities.OnlineGameShortInfoStatus
import by.klnvch.link5dots.domain.history.entities.OnlineHistoryStats
import by.klnvch.link5dots.domain.history.entities.Opponent
import by.klnvch.link5dots.domain.history.entities.Performance

internal data class OnlineHistoryStatsImpl(
    override val items: List<OnlineGameShortInfo>,
    override val totalPerformance: PerformanceImpl,
    override val performanceByUser: Map<Opponent, Performance>,
) : OnlineHistoryStats

internal data class OnlineGameShortInfoImpl(
    override val opponent: Opponent,
    override val timeText: String,           // e.g. "02:15"
    override val sizeText: String,           // e.g. "12"
    override val durationText: String,       // e.g. "Dec 18, 05:22 PM"
    override val status: OnlineGameShortInfoStatus,
) : OnlineGameShortInfo

internal class PerformanceImpl(
    override var wins: Int = 0,
    override var losses: Int = 0,
    override var draws: Int = 0,
) : Performance

internal data class OpponentImpl(
    override val id: String,
    override val name: String,
) : Opponent {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpponentImpl) return false
        return id == other.id
    }

    override fun hashCode() = id.hashCode()
}
