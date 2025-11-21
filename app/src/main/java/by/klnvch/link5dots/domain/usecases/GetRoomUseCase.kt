/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package by.klnvch.link5dots.domain.usecases

import android.util.Log
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RoomFactory
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.isNew
import by.klnvch.link5dots.domain.repositories.KeyForInfoRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomFlowLocalRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.UserNameResolver
import by.klnvch.link5dots.domain.repositories.VibratorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

class GameStateFactory @Inject constructor(
    private val userNameResolver: UserNameResolver,
) {
    fun create(room: IRoom, isActive: Boolean): GameState {
        val user1Name = userNameResolver.get(room.user1)
        val user2Name = userNameResolver.get(room.user2)
        return GameState(room, user1Name, user2Name, isActive)
    }
}

interface GetRoomUseCase {
    val room: Flow<GameState>
}

class GetRoomCommonUseCase @Inject constructor(
    getRepository: RoomFlowLocalRepository,
    scope: CoroutineScope,
    private val saveRepository: RoomSaveLocalRepository,
    private val roomFactory: RoomFactory,
    private val gameStateFactory: GameStateFactory,
) : GetRoomUseCase {
    override val room: Flow<GameState> = getRepository.roomFlow
        .distinctUntilChanged()
        .onEach {
            Log.d("GetRoomCommonUseCase", "once per updated: $it")
            if (it == null) saveRepository.save(roomFactory.generate())
        }
        .filterNotNull()
        .map { gameStateFactory.create(it, true) }
        .shareIn(scope, SharingStarted.WhileSubscribed(), 1)
}

class GetRoomInfoUseCase @Inject constructor(
    private val repository: RoomRepository,
    keyForInfoRepository: KeyForInfoRepository,
    private val gameStateFactory: GameStateFactory,
) : GetRoomUseCase {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val room: Flow<GameState> = keyForInfoRepository.key.flatMapLatest { key ->
        repository.getByKey(key).filterNotNull().map { gameStateFactory.create(it, false) }
    }
}

class VibrationCommand @Inject constructor(
    private val settings: Settings,
    private val networkUserProvider: NetworkUserProvider,
    private val vibratorService: VibratorService,
) {
    suspend fun vibrate(room: INetworkRoom) {
        val isEnabled = settings.isVibrationEnabled.first()
        val canMove = room.canMove(networkUserProvider.networkUser)
        val isNew = room.isNew()
        if (isEnabled && (canMove || isNew)) vibratorService.vibrate()
    }
}

class GetRoomSocketUseCase @Inject constructor(
    repository: RoomFlowRemoteRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val gameStateFactory: GameStateFactory,
    private val vibrationCommand: VibrationCommand,
) : GetRoomUseCase {
    override val room: Flow<GameState> = repository.roomFlow
        .onEach { vibrationCommand.vibrate(it) }
        .onEach { saveRepository.save(it) }
        .map { gameStateFactory.create(it, true) }
}

class GetRoomOnlineUseCase @Inject constructor(
    repository: RoomFlowOnlineRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val gameStateFactory: GameStateFactory,
    private val vibrationCommand: VibrationCommand,
) : GetRoomUseCase {
    override val room: Flow<GameState> = repository.onlineRoom
        .onEach { vibrationCommand.vibrate(it.room) }
        .onEach { saveRepository.save(it.room) }
        .map { gameStateFactory.create(it.room, it.isActive) }
}
