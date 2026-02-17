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
    override val timeText: String,
    override val size: Int,
    override val duration: Int,
    override val status: OnlineGameShortInfoStatus,
) : OnlineGameShortInfo

internal class PerformanceImpl(
    override var wins: Int = 0,
    override var losses: Int = 0,
    override var draws: Int = 0,
    override var duration: Int = 0,
    override var size: Int = 0,
) : Performance {
    override val total: Int = wins + losses + draws
    override val durationAvg: Int = duration / total
    override var sizeAvg: Int = size / total
}

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
