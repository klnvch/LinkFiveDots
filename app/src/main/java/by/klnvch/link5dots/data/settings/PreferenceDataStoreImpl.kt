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
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PreferenceDataStoreImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferenceDataStore() {

    override fun getString(key: String, defValue: String?): String? {
        return runBlocking {
            dataStore.data.map { it[stringPreferencesKey(key)] ?: defValue }.first()
        }
    }

    override fun putString(key: String, value: String?) {
        runBlocking {
            dataStore.edit { it[stringPreferencesKey(key)] = value ?: "" }
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.map { it[booleanPreferencesKey(key)] ?: defValue }.first()
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { it[booleanPreferencesKey(key)] = value }
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return runBlocking {
            dataStore.data.map { it[intPreferencesKey(key)] ?: defValue }.first()
        }
    }

    override fun putInt(key: String, value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { it[intPreferencesKey(key)] = value }
        }
    }
}
