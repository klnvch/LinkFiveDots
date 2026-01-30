package by.klnvch.link5dots.domain.history.usecase

import by.klnvch.link5dots.domain.history.entities.OnlineGameShortInfo
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
    suspend fun getUserHistory(): List<OnlineGameShortInfo> {
        try {
            val userId = networkUserProvider.networkUserOrThrow.id
            return repository.getUserHistory(userId)
                .map { decode(it) }
                .map { it.toOnlineGameShortInfo(stringProvider.unknownName) }
        } catch (e: Throwable) {
            println(e.message)
            return emptyList()
        }
    }
}
