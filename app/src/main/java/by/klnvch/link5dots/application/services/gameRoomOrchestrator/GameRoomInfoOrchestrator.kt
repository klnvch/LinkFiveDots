package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.OfflineGameStateFactory
import by.klnvch.link5dots.domain.repositories.KeyForInfoRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameRoomInfoOrchestrator @Inject constructor(
    private val repository: RoomRepository,
    private val keyForInfoRepository: KeyForInfoRepository,
    private val gameStateFactory: OfflineGameStateFactory,
) : GameRoomOrchestrator {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAndSync(): Flow<GameState> {
        return keyForInfoRepository.key.flatMapLatest { key ->
            repository.getByKey(key).filterNotNull().map { gameStateFactory.create(it, false) }
        }
    }
}
