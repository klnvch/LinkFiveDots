package by.klnvch.link5dots.domain.history.service

import by.klnvch.link5dots.domain.events.DomainEventBus
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.events.OnlineRoomUpdatedEvent
import by.klnvch.link5dots.domain.history.entities.encode
import by.klnvch.link5dots.domain.history.entities.mapToHistoryOnlineRoomItem
import by.klnvch.link5dots.domain.history.repository.UserHistorySaveRemoteRepository
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

class UserHistoryHandler(
    private val eventBus: DomainEventBus,
    private val repo: UserHistorySaveRemoteRepository,
    private val userProvider: NetworkUserProvider,
) : DomainHandler {
    init {
        CoroutineScope(Dispatchers.Default).launch {
            eventBus.events.filterIsInstance<OnlineRoomUpdatedEvent>().collect { event ->
                syncIfNeeded(event.previous, event.current)
            }
        }
    }

    private suspend fun syncIfNeeded(prev: OnlineRoomLive?, next: OnlineRoomLive) {
        if (!shouldUpdateHistory(prev, next)) return
        runCatching {
            val user = userProvider.networkUserOrThrow
            val item = next.mapToHistoryOnlineRoomItem(user)
            repo.addItemToUserHistory(user.id, next.room.key, item.encode())
        }.onFailure { e ->
            println(e.message)
        }
    }

    private fun shouldUpdateHistory(prev: OnlineRoomLive?, next: OnlineRoomLive): Boolean =
        prev?.room?.key != next.room.key || (prev.isActive && !next.isActive)
}
