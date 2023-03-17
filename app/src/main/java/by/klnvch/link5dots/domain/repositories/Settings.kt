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

package by.klnvch.link5dots.domain.repositories

import kotlinx.coroutines.flow.Flow

interface Settings {
    fun getUserName(): Flow<String>
    fun getUserNameBlocking(): String
    suspend fun setUserName(userName: String)
    fun isFirstRun(): Flow<Boolean>
    suspend fun setFirstRun()
    fun getDotsType(): Flow<Int>
    fun getLanguage(): Flow<String>
    fun getNightMode(): Flow<String>
    suspend fun reset()

    companion object {
        const val KEY_USER_NAME = "pref_username"
        const val KEY_FIRST_RUN = "FIRST_RUN"
        const val KEY_LANGUAGE = "pref_language"
        const val KEY_VIBRATION = "pref_vibration"
        const val KEY_DOTS_TYPE = "pref_dots_type"
        const val KEY_NIGHT_MODE = "pref_night_mode"
    }
}
