package by.klnvch.link5dots.domain.history.service

import by.klnvch.link5dots.domain.history.entities.encode
import by.klnvch.link5dots.domain.history.repository.UserHistorySaveRemoteRepository
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow

class UserHistoryService(
    private val repo: UserHistorySaveRemoteRepository,
    private val userProvider: NetworkUserProvider,
) {
    suspend fun syncHistory(prev: OnlineRoomLive?, next: OnlineRoomLive) {
        if (!shouldUpdateHistory(prev, next)) return

        runCatching {
            val user = userProvider.networkUserOrThrow
            repo.addItemToUserHistory(user.id, next.room.key, encode(user, next))
        }.onFailure { e ->
            println(e.message)
        }
    }

    private fun shouldUpdateHistory(prev: OnlineRoomLive?, next: OnlineRoomLive): Boolean =
        prev?.room?.key != next.room.key || (prev.isActive && !next.isActive)
}
