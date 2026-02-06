package by.klnvch.link5dots.di.game.online

import by.klnvch.link5dots.data.online.FirebaseDbImpl
import by.klnvch.link5dots.di.ActivityScope
import by.klnvch.link5dots.domain.events.DomainEventBus
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.history.service.UserHistoryHandler
import by.klnvch.link5dots.domain.history.usecase.GetOnlineUserHistoryUseCase
import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.repositories.AddDotOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.CreateOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.GameActionsFactory
import by.klnvch.link5dots.domain.repositories.GameActionsOnlineFactory
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.UpdateStateOnlineRoomRepository
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import by.klnvch.link5dots.domain.usecases.NewGameEmptyUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanOnlineRoomDescriptorFactory
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module
class OnlineGameRulesModule2 {
    @Provides
    fun provideNewGameUseCase(): NewGameUseCase = NewGameEmptyUseCase()

    @Provides
    fun provideGameActionsFactory(): GameActionsFactory = GameActionsOnlineFactory()

    @Provides
    fun provideCreateOnlineRoomUseCase(
        roomKeyGenerator: RoomKeyGenerator,
        networkUserProvider: NetworkUserProvider,
        createOnlineRoomRepository: CreateOnlineRoomRepository,
    ) = CreateOnlineRoomUseCase(
        roomKeyGenerator,
        networkUserProvider,
        createOnlineRoomRepository,
    )

    @Provides
    fun provideAddDotOnlineUseCase(
        getRepository: RoomRemoteRepository,
        board: Board,
        networkUserProvider: NetworkUserProvider,
        addDotRepository: AddDotOnlineRoomRepository,
        updateStateRepository: UpdateStateOnlineRoomRepository,
    ) = AddDotOnlineUseCase(
        getRepository,
        board,
        networkUserProvider,
        addDotRepository,
        updateStateRepository,
    )

    @Provides
    fun provideScanOnlineRoomDescriptorFactory(
        networkUserProvider: NetworkUserProvider,
        stringProvider: StringProvider,
    ) = ScanOnlineRoomDescriptorFactory(
        networkUserProvider,
        stringProvider,
    )

    @Provides
    @IntoSet
    @ActivityScope
    fun provideUserHistoryHandler(
        eventBus: DomainEventBus,
        repo: FirebaseDbImpl,
        networkUserProvider: NetworkUserProvider,
    ): DomainHandler = UserHistoryHandler(eventBus, repo, networkUserProvider)

    @Provides
    fun provideGetOnlineUserHistoryUseCase(
        firebaseDb: FirebaseDbImpl,
        networkUserProvider: NetworkUserProvider,
        stringProvider: StringProvider,
    ) = GetOnlineUserHistoryUseCase(firebaseDb, networkUserProvider, stringProvider)
}
