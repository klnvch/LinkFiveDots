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

package by.klnvch.link5dots.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import by.klnvch.link5dots.domain.models.DotsType
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class SettingsImpl(private val dataStore: DataStore<Preferences>) : Settings {

    companion object {
        private val USER_NAME = stringPreferencesKey(Settings.KEY_USER_NAME)
        private val LANGUAGE = stringPreferencesKey(Settings.KEY_LANGUAGE)
        private val NIGHT_MODE = stringPreferencesKey(Settings.KEY_NIGHT_MODE)
        private val FIRST_RUN = booleanPreferencesKey(Settings.KEY_FIRST_RUN)
        private val VIBRATION = booleanPreferencesKey(Settings.KEY_VIBRATION)
        private val DOTS_TYPE = intPreferencesKey(Settings.KEY_DOTS_TYPE)

        private const val DEFAULT_USER_NAME = ""
        private const val DEFAULT_LANGUAGE = ""
        private const val DEFAULT_VIBRATION = true
        private const val DEFAULT_NIGHT_MODE = ""
        private const val DEFAULT_DOTS_TYPE = DotsType.ORIGINAL
        private const val DEFAULT_FIRST_RUN = false
    }

    override suspend fun setUserName(userName: String) {
        dataStore.edit { it[USER_NAME] = userName }
    }

    override fun getUserName() = dataStore.data
        .map { it[USER_NAME] ?: DEFAULT_USER_NAME }
        .distinctUntilChanged()

    override fun getUserNameBlocking(): String {
        return runBlocking { getUserName().first() }
    }

    override fun getLanguage() = dataStore.data
        .map { it[LANGUAGE] ?: DEFAULT_LANGUAGE }
        .distinctUntilChanged()

    override suspend fun isFirstRun(): Boolean {
        return dataStore.data.map { it[FIRST_RUN] ?: DEFAULT_FIRST_RUN }.first()
    }

    override suspend fun setFirstRun() {
        dataStore.edit { it[FIRST_RUN] = true }
    }

    fun isVibrationEnabled() = dataStore.data
        .map { it[VIBRATION] ?: DEFAULT_VIBRATION }
        .distinctUntilChanged()

    override fun getDotsType() = dataStore.data
        .map { it[DOTS_TYPE] ?: DEFAULT_DOTS_TYPE }
        .distinctUntilChanged()

    override fun getNightMode() = dataStore.data
        .map { it[NIGHT_MODE] ?: DEFAULT_NIGHT_MODE }
        .distinctUntilChanged()

    override suspend fun reset() {
        dataStore.edit {
            it[USER_NAME] = DEFAULT_USER_NAME
            it[LANGUAGE] = DEFAULT_LANGUAGE
            it[VIBRATION] = DEFAULT_VIBRATION
            it[NIGHT_MODE] = DEFAULT_NIGHT_MODE
            it[DOTS_TYPE] = DEFAULT_DOTS_TYPE
            it[FIRST_RUN] = DEFAULT_FIRST_RUN
        }
    }
}
