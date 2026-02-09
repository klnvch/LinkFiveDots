package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.domain.events.DomainEventPublisher
import by.klnvch.link5dots.domain.events.NetworkRoomUpdatedEvent
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.NetworkGameStateFactory
import by.klnvch.link5dots.domain.repositories.RoomFlowRemoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GameRoomSocketOrchestrator @Inject constructor(
    private val repository: RoomFlowRemoteRepository,
    private val gameStateFactory: NetworkGameStateFactory,
    private val domainEventPublisher: DomainEventPublisher,
) : GameRoomOrchestrator {
    override fun observeAndSync(): Flow<GameState> = repository.roomFlow
        .onEach { domainEventPublisher.publish(NetworkRoomUpdatedEvent(it)) }
        .map { gameStateFactory.create(it, true) }
}
