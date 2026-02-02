package by.klnvch.link5dots.domain.history.service

import by.klnvch.link5dots.domain.history.entities.encode
import by.klnvch.link5dots.domain.history.repository.UserHistorySaveRemoteRepository
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow

class UserHistoryProxyService(
    private val repo: UserHistorySaveRemoteRepository,
    private val userProvider: NetworkUserProvider,
) {
    suspend fun addToHistory(prev: OnlineRoomLive?, next: OnlineRoomLive) {
        try {
            if (isNewGame(prev, next) || isFinished(prev, next)) {
                val user = userProvider.networkUserOrThrow
                repo.addItemToUserHistory(user.id, next.room.key, encode(user, next))
            }
        } catch (e: Exception) {
            // ignore failure silently
            println(e.message)
        }
    }

    private fun isNewGame(prev: OnlineRoomLive?, next: OnlineRoomLive): Boolean =
        prev?.room?.key != next.room.key

    private fun isFinished(old: OnlineRoomLive?, next: OnlineRoomLive): Boolean =
        old?.isActive != next.isActive
}
