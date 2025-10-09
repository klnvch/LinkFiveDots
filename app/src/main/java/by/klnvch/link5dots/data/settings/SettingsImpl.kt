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
package by.klnvch.link5dots.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import by.klnvch.link5dots.domain.models.AllSettings
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.NightMode
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

class SettingsImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : Settings {

    companion object {
        private val USER_NAME = stringPreferencesKey(Settings.KEY_USER_NAME)
        private val USER_ID = stringPreferencesKey(Settings.KEY_USER_ID)
        private val LANGUAGE = stringPreferencesKey(Settings.KEY_LANGUAGE)
        private val NIGHT_MODE = stringPreferencesKey(Settings.KEY_NIGHT_MODE)
        private val FIRST_RUN = booleanPreferencesKey(Settings.KEY_FIRST_RUN)
        private val VIBRATION = booleanPreferencesKey(Settings.KEY_VIBRATION)
        private val DOTS_TYPE = intPreferencesKey(Settings.KEY_DOTS_TYPE)

        private const val DEFAULT_LANGUAGE = ""
        private const val DEFAULT_VIBRATION = true
        private const val DEFAULT_FIRST_RUN = false
    }

    override fun getAllSettings() =
        dataStore.data
            .map {
                AllSettings(
                    it[USER_NAME],
                    it[LANGUAGE] ?: DEFAULT_LANGUAGE,
                    it[VIBRATION] ?: DEFAULT_VIBRATION,
                    SettingsMapper.Night.map(it[NIGHT_MODE]),
                    SettingsMapper.Dots.map(it[DOTS_TYPE]),
                )
            }
            .distinctUntilChanged()


    override suspend fun setUserName(userName: String?) {
        dataStore.edit { if (userName == null) it.remove(USER_NAME) else it[USER_NAME] = userName }
    }

    override suspend fun getUserName() = getUserNameFlow().first()

    override fun getUserNameFlow() = dataStore.data
        .map { it[USER_NAME] }
        .distinctUntilChanged()

    override suspend fun setLanguage(language: String) {
        dataStore.edit { it[LANGUAGE] = language }
    }

    override suspend fun setVibration(isOn: Boolean) {
        dataStore.edit { it[VIBRATION] = isOn }
    }

    override suspend fun setNightMode(mode: NightMode) {
        dataStore.edit { it[NIGHT_MODE] = SettingsMapper.Night.map(mode) }
    }

    override suspend fun setDotsStyle(style: DotsStyleType) {
        dataStore.edit { it[DOTS_TYPE] = SettingsMapper.Dots.map(style) }
    }

    override fun getUserId() = dataStore.data
        .map {
            val userId = it[USER_ID]
            if (userId == null) {
                val newId = UUID.randomUUID().toString()
                setUserId(newId)
                newId
            } else userId
        }
        .distinctUntilChanged()

    override fun getLanguage() = dataStore.data
        .map { it[LANGUAGE] ?: DEFAULT_LANGUAGE }
        .distinctUntilChanged()

    override fun isFirstRun() = dataStore.data.map { it[FIRST_RUN] ?: DEFAULT_FIRST_RUN }

    override suspend fun setFirstRun() {
        dataStore.edit { it[FIRST_RUN] = true }
    }

    fun isVibrationEnabled() = dataStore.data
        .map { it[VIBRATION] ?: DEFAULT_VIBRATION }
        .distinctUntilChanged()

    override fun getDotsType() = dataStore.data
        .map { SettingsMapper.Dots.map(it[DOTS_TYPE]) }
        .distinctUntilChanged()

    @OptIn(DelicateCoroutinesApi::class)
    override val nightMode = dataStore.data
        .map { SettingsMapper.Night.map(it[NIGHT_MODE]) }
        .stateIn(GlobalScope, SharingStarted.Eagerly, NightMode.System)

    override suspend fun reset() {
        dataStore.edit {
            it.remove(USER_NAME)
            it[USER_ID] = UUID.randomUUID().toString()
            it[LANGUAGE] = DEFAULT_LANGUAGE
            it[VIBRATION] = DEFAULT_VIBRATION
            it.remove(NIGHT_MODE)
            it[DOTS_TYPE] = SettingsMapper.Dots.map(null as DotsStyleType?)
            it[FIRST_RUN] = DEFAULT_FIRST_RUN
        }
    }

    private suspend fun setUserId(userId: String) {
        dataStore.edit { it[USER_ID] = userId }
    }
}
