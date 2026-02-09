package by.klnvch.link5dots.di.game

import by.klnvch.link5dots.application.handlers.GameNotificationHandler
import by.klnvch.link5dots.application.handlers.NetworkRoomPersistenceHandler
import by.klnvch.link5dots.application.services.gameRoomOrchestrator.GameRoomOrchestrator
import by.klnvch.link5dots.application.services.gameRoomOrchestrator.GameRoomSocketOrchestrator
import by.klnvch.link5dots.di.ActivityScope
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.repositories.GameActionsFactory
import by.klnvch.link5dots.domain.repositories.GameActionsSocketFactory
import by.klnvch.link5dots.domain.repositories.NetworkUserLocalProvider
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomCleanRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomStateRemoteRepository
import by.klnvch.link5dots.domain.repositories.SocketCreateRepository
import by.klnvch.link5dots.domain.repositories.SocketRoomInvitationRepository
import by.klnvch.link5dots.domain.repositories.SocketRoomRepository
import by.klnvch.link5dots.domain.repositories.SocketSendRepository
import by.klnvch.link5dots.domain.usecases.AddDotSocketUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.NewGameSocketUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreEmptyUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveSocketUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.CommonScanUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateSocketRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
interface SocketGameRulesModule {
    @Binds
    fun bindGameRoomOrchestrator(impl: GameRoomSocketOrchestrator): GameRoomOrchestrator

    @Binds
    fun bindNewGameUseCase(impl: NewGameSocketUseCase): NewGameUseCase

    @Binds
    fun bindAddDotUseCase(impl: AddDotSocketUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveSocketUseCase): UndoMoveUseCase

    @Binds
    fun bindSaveScoreUseCase(impl: SaveScoreEmptyUseCase): SaveScoreUseCase

    @Binds
    fun bindCreateMultiplayerRoomUseCase(impl: CreateSocketRoomUseCase): CreateMultiplayerRoomUseCase

    @Binds
    fun bindScanUseCase(impl: CommonScanUseCase): ScanUseCase

    @Binds
    fun bindNetworkUserProvider(impl: NetworkUserLocalProvider): NetworkUserProvider

    @Binds
    fun bindGameActionsFactory(impl: GameActionsSocketFactory): GameActionsFactory

    @Binds
    fun bindSocketGetFlowRepository(impl: SocketRoomRepository): RoomFlowRemoteRepository

    @Binds
    fun bindNetworkRoomStateRepository(impl: SocketRoomRepository): RoomStateRemoteRepository

    @Binds
    fun bindRoomCleanRemoteRepository(impl: SocketRoomRepository): RoomCleanRemoteRepository

    @Binds
    fun bindSocketGetRepository(impl: SocketRoomRepository): RoomRemoteRepository

    @Binds
    fun bindSocketCreateRepository(impl: SocketRoomRepository): SocketCreateRepository

    @Binds
    fun bindSocketSendRepository(impl: SocketRoomRepository): SocketSendRepository

    @Binds
    fun bindSocketRoomInvitationRepository(impl: SocketRoomRepository): SocketRoomInvitationRepository

    @Binds
    @IntoSet
    @ActivityScope
    fun bindGameNotificationHandler(impl: GameNotificationHandler): DomainHandler

    @Binds
    @IntoSet
    @ActivityScope
    fun bindNetworkRoomPersistenceHandler(impl: NetworkRoomPersistenceHandler): DomainHandler
}
