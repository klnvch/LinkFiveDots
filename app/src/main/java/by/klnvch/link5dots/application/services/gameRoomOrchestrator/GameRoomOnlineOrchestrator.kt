package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.domain.events.DomainEventPublisher
import by.klnvch.link5dots.domain.events.NetworkRoomUpdatedEvent
import by.klnvch.link5dots.domain.events.OnlineRoomChangedEvent
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.NetworkGameStateFactory
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GameRoomOnlineOrchestrator @Inject constructor(
    private val repository: RoomFlowOnlineRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val gameStateFactory: NetworkGameStateFactory,
    private val domainEventPublisher: DomainEventPublisher,
) : GameRoomOrchestrator {
    private var prev: OnlineRoomLive? = null

    override fun observeAndSync(): Flow<GameState> = repository.onlineRoom
        .onEach {
            saveRepository.save(it.room)

            domainEventPublisher.publish(NetworkRoomUpdatedEvent(it.room))
            
            domainEventPublisher.publish(OnlineRoomChangedEvent(previous = prev, current = it))
            prev = it
        }
        .map {
            gameStateFactory.create(it.room, it.isActive)
        }
}
