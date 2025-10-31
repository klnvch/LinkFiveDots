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

import android.bluetooth.BluetoothManager
import android.content.Context
import by.klnvch.link5dots.data.RoomKeyGeneratorImpl
import by.klnvch.link5dots.data.TimeServiceImpl
import by.klnvch.link5dots.data.online.AddDotOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.ConnectOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.CreateOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbImpl
import by.klnvch.link5dots.data.online.OnlineLocalStore
import by.klnvch.link5dots.data.online.OnlineLocalStoreWriter
import by.klnvch.link5dots.data.online.ScanOnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.online.UpdateStateOnlineRoomRepositoryImpl
import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.repositories.AddDotOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.ConnectOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.CreateOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.ScanOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.repositories.StringRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.repositories.UpdateStateOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.UserNameSettings
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    fun provideScanOnlineRoomRepository(
        connectRepository: ConnectOnlineRoomRepository,
    ): ScanOnlineRoomRepository = ScanOnlineRoomRepositoryImpl(connectRepository)

    @Singleton
    @Provides
    fun provideStringProvider(stringRepository: StringRepository): StringProvider =
        stringRepository

    @Singleton
    @Provides
    fun provideOnlineLocalStoreWriter(store: OnlineLocalStore): OnlineLocalStoreWriter = store

    @Singleton
    @Provides
    fun provideCreateOnlineRoomRepository(
        firebaseDb: FirebaseDbImpl,
        onlineLocalStore: OnlineLocalStore,
    ): CreateOnlineRoomRepository = CreateOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)

    @Singleton
    @Provides
    fun provideConnectOnlineRoomRepository(
        firebaseDb: FirebaseDbImpl,
        onlineLocalStore: OnlineLocalStore,
    ): ConnectOnlineRoomRepository = ConnectOnlineRoomRepositoryImpl(firebaseDb, onlineLocalStore)

    @Singleton
    @Provides
    fun provideAddDotOnlineRoomRepository(
        firebaseDb: FirebaseDbImpl,
    ): AddDotOnlineRoomRepository = AddDotOnlineRoomRepositoryImpl(firebaseDb)

    @Singleton
    @Provides
    fun provideUpdateStateOnlineRoomRepository(
        firebaseDb: FirebaseDbImpl,
    ): UpdateStateOnlineRoomRepository = UpdateStateOnlineRoomRepositoryImpl(firebaseDb)

    @Singleton
    @Provides
    fun provideBoard() = Board()

    @Singleton
    @Provides
    fun provideBluetoothManager(context: Context): BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
