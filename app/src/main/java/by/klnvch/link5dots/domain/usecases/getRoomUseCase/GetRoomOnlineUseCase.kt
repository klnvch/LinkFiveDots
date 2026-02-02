package by.klnvch.link5dots.domain.usecases.getRoomUseCase

import by.klnvch.link5dots.domain.history.service.UserHistoryProxyService
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.usecases.getRoomUseCase.utils.onEachWithPrev
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Listen for remote game changes.
 * Side effects:
 *  - vibrate on game change
 *  - save it locally
 *  - save it remotely
 */
class GetRoomOnlineUseCase @Inject constructor(
    repository: RoomFlowOnlineRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val gameStateFactory: NetworkGameStateFactory,
    private val vibrationCommand: VibrationCommand,
    private val userHistoryService: UserHistoryProxyService,
) : GetRoomUseCase {
    override val room: Flow<GameState> = repository.onlineRoom
        .onEach { vibrationCommand.vibrate(it.room) }
        .onEach { saveRepository.save(it.room) }
        .onEachWithPrev { prev, next -> userHistoryService.addToHistory(prev, next) }
        .map { gameStateFactory.create(it.room, it.isActive) }
}
