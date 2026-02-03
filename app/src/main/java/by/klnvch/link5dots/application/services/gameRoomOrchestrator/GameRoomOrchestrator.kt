package by.klnvch.link5dots.application.services.gameRoomOrchestrator

import by.klnvch.link5dots.domain.models.GameState
import kotlinx.coroutines.flow.Flow

interface GameRoomOrchestrator {
    fun observeAndSync(): Flow<GameState>
}
