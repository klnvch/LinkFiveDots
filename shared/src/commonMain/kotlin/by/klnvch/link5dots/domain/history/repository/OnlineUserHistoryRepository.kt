package by.klnvch.link5dots.domain.history.repository

import by.klnvch.link5dots.domain.history.entities.encode
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow

class OnlineUserHistoryRepository(
    private val repository: UserHistorySaveRemoteRepository,
    private val networkUserProvider: NetworkUserProvider,
) {
    suspend fun save(old: OnlineRoomLive?, new: OnlineRoomLive) {
        val isNewGameStarted = old == null || old.room.key != new.room.key
        val isGameFinished = old?.isActive != new.isActive

        if (isNewGameStarted || isGameFinished) {
            try {
                val user = networkUserProvider.networkUserOrThrow
                val value = encode(user, new)
                repository.addItemToUserHistory(user.id, new.room.key, value)
            } catch (e: Throwable) {
                println(e.message)
            }
        }
    }
}
