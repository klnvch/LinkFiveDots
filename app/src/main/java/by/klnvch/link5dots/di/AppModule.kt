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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceDataStore
import androidx.room.Room
import by.klnvch.link5dots.data.ActivitiesMemoryImpl
import by.klnvch.link5dots.data.CrashRepositoryImpl
import by.klnvch.link5dots.data.DeviceInfoImpl
import by.klnvch.link5dots.data.NightModeManagerImpl
import by.klnvch.link5dots.data.db.AppDatabase
import by.klnvch.link5dots.data.firebase.FirebaseManagerImpl
import by.klnvch.link5dots.data.firebase.GameScoreRemoteSourceImpl
import by.klnvch.link5dots.data.settings.PreferenceDataStoreImpl
import by.klnvch.link5dots.data.settings.SettingsImpl
import by.klnvch.link5dots.data.network.NetworkService
import by.klnvch.link5dots.data.network.RoomRemoteSourceImpl
import by.klnvch.link5dots.domain.repositories.*
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
class AppModule {

    @ApplicationScope
    @Provides
    fun provideNetworkService(): NetworkService {
        return Retrofit.Builder()
            .baseUrl("https://link-five-dots.firebaseio.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(NetworkService::class.java)
    }

    @ApplicationScope
    @Provides
    fun provideRoomRemoteSource(networkService: NetworkService): RoomRemoteSource =
        RoomRemoteSourceImpl(networkService)

    @ApplicationScope
    @Provides
    fun provideDeviceInfo(app: Application): DeviceInfo = DeviceInfoImpl(app)

    @ApplicationScope
    @Provides
    fun provideDatabase(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
    }

    @ApplicationScope
    @Provides
    fun provideRoomDao(appDatabase: AppDatabase): RoomDao = appDatabase.roomDao()

    @ApplicationScope
    @Provides
    fun provideSettings(app: Application): Settings = SettingsImpl(app.dataStore)

    @ApplicationScope
    @Provides
    fun providePreferenceDataStore(app: Application): PreferenceDataStore =
        PreferenceDataStoreImpl(app.dataStore)

    @ApplicationScope
    @Provides
    fun provideActivitiesMemory(app: Application): ActivitiesMemory = ActivitiesMemoryImpl(app)

    @ApplicationScope
    @Provides
    fun provideNighModeManager(): NightModeManager = NightModeManagerImpl()

    @ApplicationScope
    @Provides
    fun provideCrashRepository(): CrashRepository = CrashRepositoryImpl()

    @ApplicationScope
    @Provides
    fun provideContext(app: Application): Context = app

    @ApplicationScope
    @Provides
    fun provideFirebaseManager(app: Application): FirebaseManager = FirebaseManagerImpl(app)

    @ApplicationScope
    @Provides
    fun provideGameScoreRemoteSource(): GameScoreRemoteSource = GameScoreRemoteSourceImpl()
}
