package by.klnvch.link5dots.domain.usecases.getRoomUseCase

import by.klnvch.link5dots.domain.history.repository.OnlineUserHistoryRepository
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetRoomOnlineUseCase @Inject constructor(
    repository: RoomFlowOnlineRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val gameStateFactory: NetworkGameStateFactory,
    private val vibrationCommand: VibrationCommand,
    private val userHistoryRepository: OnlineUserHistoryRepository,
) : GetRoomUseCase {
    override val room: Flow<GameState> = repository.onlineRoom
        .onEach { vibrationCommand.vibrate(it.room) }
        .onEach { saveRepository.save(it.room) }
        .onEachWithPrevious { old, new -> userHistoryRepository.save(old, new) }
        .map { gameStateFactory.create(it.room, it.isActive) }
}
