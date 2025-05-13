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
import android.bluetooth.BluetoothServerSocket
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.FAKE_ADDRESS
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.NAME_SECURE
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.UUID_SECURE

object BluetoothExt {
    fun BluetoothManager.getDeviceName(): String {
        return adapter.name ?: ""
    }

    fun BluetoothManager.getDeviceAddress(): String {
        val address = adapter.address
        return if (address == FAKE_ADDRESS) "" else address
    }

    fun BluetoothManager.createServerSocket(): BluetoothServerSocket =
        adapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE)

    val BluetoothManager.bondedDevices: Set<BluetoothDevice> get() = adapter.bondedDevices

    val BluetoothDevice.deviceName: String? get() = name

    val BluetoothDevice.isBonded get() = bondState == BluetoothDevice.BOND_BONDED
}
