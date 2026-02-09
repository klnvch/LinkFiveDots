package by.klnvch.link5dots.application.handlers

import by.klnvch.link5dots.domain.events.DomainEventBus
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.events.NetworkRoomUpdatedEvent
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkRoomPersistenceHandler @Inject constructor(
    private val eventBus: DomainEventBus,
    private val repository: RoomSaveLocalRepository,
) : DomainHandler {
    init {
        CoroutineScope(Dispatchers.Default).launch {
            eventBus.events.filterIsInstance<NetworkRoomUpdatedEvent>().collect { event ->
                handle(event.room)
            }
        }
    }

    private suspend fun handle(room: INetworkRoom) {
        repository.save(room)
    }
}
