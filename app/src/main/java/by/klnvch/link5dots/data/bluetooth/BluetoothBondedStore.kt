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

package by.klnvch.link5dots.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.bondedDevices
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.deviceName
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class BluetoothBondedStore @Inject constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bluetooth")
    private val bondedDevices = stringSetPreferencesKey("bonded")
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)

    suspend fun save(device: BluetoothDevice) {
        context.dataStore.edit {
            val set = it[bondedDevices] ?: emptySet()
            it[bondedDevices] = set.plus(device.address)
        }
    }

    suspend fun remove(address: String) {
        context.dataStore.edit {
            val set = it[bondedDevices] ?: emptySet()
            it[bondedDevices] = set.minus(address)
        }
    }

    suspend fun getKnown(): List<BluetoothDevice> {
        val bonded = bluetoothManager.bondedDevices
        val savedAddresses = context.dataStore.data.map { it[bondedDevices] ?: emptySet() }.first()
        // clean storage
        val bondedAddresses = bonded.map { it.address }
        for (address in savedAddresses) {
            if (!bondedAddresses.contains(address)) {
                remove(address)
            }
        }
        // find stored devices
        return bonded
            .filter { savedAddresses.contains(it.address) }
            .toList()
            .sortedWith(compareBy { it.deviceName })
    }
}
