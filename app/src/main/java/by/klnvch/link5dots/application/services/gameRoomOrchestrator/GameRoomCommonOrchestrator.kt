package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.OfflineGameStateFactory
import by.klnvch.link5dots.domain.models.RoomFactory
import by.klnvch.link5dots.domain.repositories.RoomFlowLocalRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class GameRoomCommonOrchestrator @Inject constructor(
    private val getRepository: RoomFlowLocalRepository,
    private val scope: CoroutineScope,
    private val saveRepository: RoomSaveLocalRepository,
    private val roomFactory: RoomFactory,
    private val gameStateFactory: OfflineGameStateFactory,
) : GameRoomOrchestrator {
    override fun observeAndSync(): Flow<GameState> {
        return getRepository.roomFlow
            .distinctUntilChanged()
            .onEach { if (it == null) saveRepository.save(roomFactory.generate()) }
            .filterNotNull()
            .map { gameStateFactory.create(it, true) }
            .shareIn(scope, SharingStarted.WhileSubscribed(), 1)
    }
}
