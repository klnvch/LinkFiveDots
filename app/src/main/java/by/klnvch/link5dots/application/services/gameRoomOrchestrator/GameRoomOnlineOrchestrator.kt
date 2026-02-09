package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.domain.events.DomainEventPublisher
import by.klnvch.link5dots.domain.events.NetworkRoomUpdatedEvent
import by.klnvch.link5dots.domain.events.OnlineRoomUpdatedEvent
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.NetworkGameStateFactory
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import javax.inject.Inject

/**
 * Orchestrates the online game room state.
 *
 * Observes [RoomFlowOnlineRepository.onlineRoom] and:
 * - Publishes domain events for changes.
 * - Transforms the remote `OnlineRoomLive` into a local [GameState].
 */
class GameRoomOnlineOrchestrator @Inject constructor(
    private val repository: RoomFlowOnlineRepository,
    private val gameStateFactory: NetworkGameStateFactory,
    private val domainEventPublisher: DomainEventPublisher,
) : GameRoomOrchestrator {
    override fun observeAndSync(): Flow<GameState> = repository.onlineRoom
        .runningFold(null as OnlineRoomLive?) { previous, current ->
            domainEventPublisher.publish(NetworkRoomUpdatedEvent(current.room))
            domainEventPublisher.publish(OnlineRoomUpdatedEvent(previous, current))
            current
        }
        .filterNotNull()
        .map { roomLive ->
            gameStateFactory.create(roomLive.room, roomLive.isActive)
        }
}
