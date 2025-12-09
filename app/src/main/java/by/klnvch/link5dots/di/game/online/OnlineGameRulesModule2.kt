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
package by.klnvch.link5dots.di.game.online

import by.klnvch.link5dots.data.online.FirebaseDbImpl
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
import by.klnvch.link5dots.domain.repositories.online.OnlineUserHistoryRepository
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import by.klnvch.link5dots.domain.usecases.NewGameEmptyUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanOnlineRoomDescriptorFactory
import dagger.Module
import dagger.Provides

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
    fun provideOnlineUserHistoryRepository(
        firebaseDb: FirebaseDbImpl,
        networkUserProvider: NetworkUserProvider,
    ) = OnlineUserHistoryRepository(firebaseDb, networkUserProvider)
}
