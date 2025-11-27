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

package by.klnvch.link5dots.domain.repositories

import by.klnvch.link5dots.domain.models.AllSettings
import by.klnvch.link5dots.domain.models.DotsStyle
import by.klnvch.link5dots.domain.models.NightMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Settings : UserNameSettings {
    fun getAllSettings(): Flow<AllSettings>
    fun getUserNameFlow(): Flow<String?>
    val userId: Flow<String>
    suspend fun setUserName(userName: String?)
    suspend fun setLanguage(language: String)
    suspend fun setVibration(isOn: Boolean)
    suspend fun setNightMode(mode: NightMode)
    suspend fun setDotsStyle(dotsStyle: DotsStyle)
    fun isFirstRun(): Flow<Boolean>
    suspend fun setFirstRun()
    val dotsStyle: StateFlow<DotsStyle>
    fun getLanguage(): Flow<String>
    val isVibrationEnabled: Flow<Boolean>
    val nightMode: StateFlow<NightMode>
    suspend fun reset()

    companion object {
        const val KEY_USER_NAME = "pref_username"
        const val KEY_USER_ID = "pref_user_id"
        const val KEY_FIRST_RUN = "FIRST_RUN"
        const val KEY_LANGUAGE = "pref_language"
        const val KEY_VIBRATION = "pref_vibration"
        const val KEY_DOTS_TYPE = "pref_dots_type"
        const val KEY_NIGHT_MODE = "pref_night_mode"
    }
}
