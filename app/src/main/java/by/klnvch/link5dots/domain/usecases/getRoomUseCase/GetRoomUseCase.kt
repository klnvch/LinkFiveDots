package by.klnvch.link5dots.domain.usecases.getRoomUseCase

import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.GameStateFactory
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RoomFactory
import by.klnvch.link5dots.domain.models.canMove
import by.klnvch.link5dots.domain.models.isNew
import by.klnvch.link5dots.domain.repositories.AnyUserNameResolver
import by.klnvch.link5dots.domain.repositories.GameActionsFactory
import by.klnvch.link5dots.domain.repositories.KeyForInfoRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserNameResolver
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomFlowLocalRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.Settings
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

class OfflineGameStateFactory @Inject constructor(
    userNameResolver: AnyUserNameResolver,
    gameActionsFactory: GameActionsFactory,
) : GameStateFactory<IRoom>(userNameResolver, gameActionsFactory)

class NetworkGameStateFactory @Inject constructor(
    userNameResolver: NetworkUserNameResolver,
    gameActionsFactory: GameActionsFactory,
) : GameStateFactory<INetworkRoom>(userNameResolver, gameActionsFactory)

interface GetRoomUseCase {
    val room: Flow<GameState>
}

class GetRoomCommonUseCase @Inject constructor(
    getRepository: RoomFlowLocalRepository,
    scope: CoroutineScope,
    private val saveRepository: RoomSaveLocalRepository,
    private val roomFactory: RoomFactory,
    private val gameStateFactory: OfflineGameStateFactory,
) : GetRoomUseCase {
    override val room: Flow<GameState> = getRepository.roomFlow
        .distinctUntilChanged()
        .onEach { if (it == null) saveRepository.save(roomFactory.generate()) }
        .filterNotNull()
        .map { gameStateFactory.create(it, true) }
        .shareIn(scope, SharingStarted.WhileSubscribed(), 1)
}

class GetRoomInfoUseCase @Inject constructor(
    private val repository: RoomRepository,
    keyForInfoRepository: KeyForInfoRepository,
    private val gameStateFactory: OfflineGameStateFactory,
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
    private val gameStateFactory: NetworkGameStateFactory,
    private val vibrationCommand: VibrationCommand,
) : GetRoomUseCase {
    override val room: Flow<GameState> = repository.roomFlow
        .onEach { vibrationCommand.vibrate(it) }
        .onEach { saveRepository.save(it) }
        .map { gameStateFactory.create(it, true) }
}
