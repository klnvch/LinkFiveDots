package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.application.services.GameNotificationOrchestrator
import by.klnvch.link5dots.domain.history.service.UserHistoryService
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.NetworkGameStateFactory
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.utils.onEachWithPrev
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GameRoomOnlineOrchestrator @Inject constructor(
    private val repository: RoomFlowOnlineRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val gameStateFactory: NetworkGameStateFactory,
    private val notifier: GameNotificationOrchestrator,
    private val userHistoryService: UserHistoryService,
) : GameRoomOrchestrator {
    override fun observeAndSync(): Flow<GameState> = repository.onlineRoom
        .onEach {
            notifier.notifyIfRequired(it.room)
            saveRepository.save(it.room)
        }
        .onEachWithPrev { prev, next ->
            userHistoryService.syncHistory(prev, next)
        }
        .map {
            gameStateFactory.create(it.room, it.isActive)
        }
}
