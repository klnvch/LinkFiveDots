/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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
import androidx.preference.PreferenceDataStore
import by.klnvch.link5dots.data.*
import by.klnvch.link5dots.data.firebase.AnalyticsImpl
import by.klnvch.link5dots.data.firebase.FirebaseManagerImpl
import by.klnvch.link5dots.data.GameScoreRepositoryImpl
import by.klnvch.link5dots.data.settings.PreferenceDataStoreImpl
import by.klnvch.link5dots.data.settings.SettingsImpl
import by.klnvch.link5dots.domain.repositories.*
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
    fun bindPreferenceDataStore(impl: PreferenceDataStoreImpl): PreferenceDataStore

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
}
