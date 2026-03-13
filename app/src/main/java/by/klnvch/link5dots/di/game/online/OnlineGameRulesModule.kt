package by.klnvch.link5dots.di.game.online

import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.application.handlers.GameNotificationHandler
import by.klnvch.link5dots.application.handlers.NetworkRoomPersistenceHandler
import by.klnvch.link5dots.application.services.gameRoomOrchestrator.GameRoomOnlineOrchestrator
import by.klnvch.link5dots.application.services.gameRoomOrchestrator.GameRoomOrchestrator
import by.klnvch.link5dots.data.online.OnlineRoomRepositoryImpl
import by.klnvch.link5dots.di.ActivityScope
import by.klnvch.link5dots.di.viewmodels.ViewModelKey
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.models.RoomTypeOnlineProvider
import by.klnvch.link5dots.domain.models.RoomTypeProvider
import by.klnvch.link5dots.domain.repositories.NetworkUserFirebaseProvider
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomCleanRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomStateRemoteRepository
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreEmptyUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveEmptyUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.InitOnlineUseCase
import by.klnvch.link5dots.domain.usecases.network.OnlineScanUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import by.klnvch.link5dots.ui.game.picker.FirebaseStatusViewModel
import by.klnvch.link5dots.ui.game.viewmodels.OnlineUserHistoryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

@Module
interface OnlineGameRulesModule {
    @Binds
    fun bindGameRoomOrchestrator(impl: GameRoomOnlineOrchestrator): GameRoomOrchestrator

    @Binds
    fun bindRoomTypeProvider(impl: RoomTypeOnlineProvider): RoomTypeProvider

    @Binds
    fun bindOnlineRoomRepository(impl: OnlineRoomRepositoryImpl): OnlineRoomRepository

    @Binds
    fun bindNetworkRoomStateRepository(impl: OnlineRoomRepository): RoomStateRemoteRepository

    @Binds
    fun bindAddDotUseCase(impl: AddDotOnlineUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveEmptyUseCase): UndoMoveUseCase

    @Binds
    fun bindSaveScoreUseCase(impl: SaveScoreEmptyUseCase): SaveScoreUseCase

    @Binds
    fun bindInitMultiplayerUseCase(impl: InitOnlineUseCase): InitMultiplayerUseCase

    @Binds
    fun bindCreateMultiplayerRoomUseCase(impl: CreateOnlineRoomUseCase): CreateMultiplayerRoomUseCase

    @Binds
    fun bindScanUseCase(impl: OnlineScanUseCase): ScanUseCase

    @Binds
    fun bindRoomCleanRemoteRepository(impl: OnlineRoomRepository): RoomCleanRemoteRepository

    @Binds
    fun bindNetworkUserProvider(impl: NetworkUserFirebaseProvider): NetworkUserProvider

    @Binds
    fun bindRoomFlowOnlineRepository(impl: OnlineRoomRepository): RoomFlowOnlineRepository

    @Binds
    fun bindRoomRemoteRepository(impl: OnlineRoomRepository): RoomRemoteRepository

    @Binds
    @IntoMap
    @ViewModelKey(FirebaseStatusViewModel::class)
    fun bindFirebaseStatusViewModel(viewModel: FirebaseStatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnlineUserHistoryViewModel::class)
    fun bindOnlineUserHistoryViewModel(viewModel: OnlineUserHistoryViewModel): ViewModel

    @Binds
    @IntoSet
    @ActivityScope
    fun bindGameNotificationHandler(impl: GameNotificationHandler): DomainHandler

    @Binds
    @IntoSet
    @ActivityScope
    fun bindNetworkRoomPersistenceHandler(impl: NetworkRoomPersistenceHandler): DomainHandler
}
