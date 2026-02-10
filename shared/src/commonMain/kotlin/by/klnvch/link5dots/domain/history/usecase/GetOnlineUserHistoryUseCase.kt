package by.klnvch.link5dots.domain.history.usecase

import by.klnvch.link5dots.domain.history.entities.OnlineHistoryStats
import by.klnvch.link5dots.domain.history.entities.calculateStats
import by.klnvch.link5dots.domain.history.entities.decode
import by.klnvch.link5dots.domain.history.entities.toOnlineGameShortInfo
import by.klnvch.link5dots.domain.history.repository.UserHistoryReadRemoteRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow

class GetOnlineUserHistoryUseCase(
    private val repository: UserHistoryReadRemoteRepository,
    private val networkUserProvider: NetworkUserProvider,
    private val stringProvider: StringProvider,
) {
    suspend fun getUserHistory(): OnlineHistoryStats {
        try {
            val userId = networkUserProvider.networkUserOrThrow.id
            val items = repository.getUserHistory(userId)
                .map { decode(it) }
                .map { it.toOnlineGameShortInfo(stringProvider.unknownName) }
            return calculateStats(items)
        } catch (e: Throwable) {
            println(e.message)
            return calculateStats(emptyList())
        }
    }
}
