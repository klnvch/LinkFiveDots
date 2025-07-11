/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.di

import by.klnvch.link5dots.data.RoomKeyGeneratorImpl
import by.klnvch.link5dots.data.TimeServiceImpl
import by.klnvch.link5dots.data.online.ConnectOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.CreateOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbSet
import by.klnvch.link5dots.data.online.FirebaseDbUpdate
import by.klnvch.link5dots.data.online.OnlineLocalStore
import by.klnvch.link5dots.data.online.OnlineLocalStoreWriter
import by.klnvch.link5dots.domain.repositories.ConnectOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.CreateOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.FirebaseAuthManager
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.UserNameSettings
import by.klnvch.link5dots.domain.usecases.network.ConnectOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppBindingModule2 {
    @Singleton
    @Provides
    fun provideTimeService(): TimeService = TimeServiceImpl()

    @Singleton
    @Provides
    fun provideRoomKeyGenerator(timeService: TimeService): RoomKeyGenerator =
        RoomKeyGeneratorImpl(timeService)

    @Singleton
    @Provides
    fun provideUserNameSettings(settings: Settings): UserNameSettings = settings

    @Singleton
    @Provides
    fun provideFirebaseAuthManager(manager: FirebaseManager): FirebaseAuthManager = manager

    @Singleton
    @Provides
    fun provideCreateOnlineRoomUseCase(
        userNameSettings: UserNameSettings,
        timeService: TimeService,
        roomKeyGenerator: RoomKeyGenerator,
        firebaseAuthManager: FirebaseAuthManager,
        createOnlineRoomRepository: CreateOnlineRoomRepository,
    ) = CreateOnlineRoomUseCase(
        userNameSettings,
        timeService,
        roomKeyGenerator,
        firebaseAuthManager,
        createOnlineRoomRepository,
    )

    @Singleton
    @Provides
    fun provideConnectOnlineRoomUseCase(
        userNameSettings: UserNameSettings,
        firebaseAuthManager: FirebaseAuthManager,
        connectOnlineRoomRepository: ConnectOnlineRoomRepository,
    ) = ConnectOnlineRoomUseCase(
        userNameSettings,
        firebaseAuthManager,
        connectOnlineRoomRepository,
    )

    @Singleton
    @Provides
    fun provideOnlineLocalStoreWriter(store: OnlineLocalStore): OnlineLocalStoreWriter = store

    @Singleton
    @Provides
    fun provideCreateOnlineRoomRepository(
        firebaseDb: FirebaseDbSet,
        onlineLocalStore: OnlineLocalStore,
    ): CreateOnlineRoomRepository = CreateOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)

    @Singleton
    @Provides
    fun provideConnectOnlineRoomRepository(
        firebaseDb: FirebaseDbUpdate,
        onlineLocalStore: OnlineLocalStore,
    ): ConnectOnlineRoomRepository = ConnectOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)
}
