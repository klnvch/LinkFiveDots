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

package by.klnvch.link5dots.di

import android.app.Application
import android.content.Context
import by.klnvch.link5dots.data.CrashRepositoryImpl
import by.klnvch.link5dots.data.DeviceInfoImpl
import by.klnvch.link5dots.data.GameScoreRepositoryImpl
import by.klnvch.link5dots.data.LanguageManagerImpl
import by.klnvch.link5dots.data.NightModeManagerImpl
import by.klnvch.link5dots.data.RoomRepositoryImpl
import by.klnvch.link5dots.data.StringProvider
import by.klnvch.link5dots.data.bluetooth.BluetoothRoomRepositoryImpl
import by.klnvch.link5dots.data.firebase.AnalyticsImpl
import by.klnvch.link5dots.data.firebase.FirebaseManagerImpl
import by.klnvch.link5dots.data.nsd.NsdRoomRepositoryImpl
import by.klnvch.link5dots.data.online.FirebaseDbImpl
import by.klnvch.link5dots.data.online.FirebaseDbSet
import by.klnvch.link5dots.data.online.FirebaseDbUpdate
import by.klnvch.link5dots.data.online.OnlineRoomRepositoryImpl
import by.klnvch.link5dots.data.settings.SettingsImpl
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.CrashRepository
import by.klnvch.link5dots.domain.repositories.DeviceInfo
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.GameScoreRepository
import by.klnvch.link5dots.domain.repositories.LanguageManager
import by.klnvch.link5dots.domain.repositories.NightModeManager
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.StringRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface AppBindingModule {

    @Singleton
    @Binds
    fun bindDeviceInfo(impl: DeviceInfoImpl): DeviceInfo

    @Singleton
    @Binds
    fun bindSettings(impl: SettingsImpl): Settings

    @Singleton
    @Binds
    fun bindNighModeManager(impl: NightModeManagerImpl): NightModeManager

    @Singleton
    @Binds
    fun bindLanguageManager(bind: LanguageManagerImpl): LanguageManager

    @Singleton
    @Binds
    fun bindCrashRepository(impl: CrashRepositoryImpl): CrashRepository

    @Singleton
    @Binds
    fun bindContext(app: Application): Context

    @Singleton
    @Binds
    fun bindFirebaseManager(impl: FirebaseManagerImpl): FirebaseManager

    @Singleton
    @Binds
    fun bindGameScoreRemoteSource(impl: GameScoreRepositoryImpl): GameScoreRepository

    @Singleton
    @Binds
    fun bindAnalytics(impl: AnalyticsImpl): Analytics

    @Singleton
    @Binds
    fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Singleton
    @Binds
    fun bindStringRepository(impl: StringProvider): StringRepository

    @Singleton
    @Binds
    fun bindOnlineRoomRepository(impl: OnlineRoomRepositoryImpl): OnlineRoomRepository

    @Singleton
    @Binds
    fun bindNsdRoomRepository(impl: NsdRoomRepositoryImpl): NsdRoomRepository

    @Singleton
    @Binds
    fun bindBluetoothRoomRepository(impl: BluetoothRoomRepositoryImpl): BluetoothRoomRepository

    @Singleton
    @Binds
    fun bindFirebaseDbSet(impl: FirebaseDbImpl): FirebaseDbSet

    @Singleton
    @Binds
    fun bindFirebaseDbUpdate(impl: FirebaseDbImpl): FirebaseDbUpdate
}
