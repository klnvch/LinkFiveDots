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

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.FAKE_ADDRESS
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.NAME_SECURE
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
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

    @SuppressLint("MissingPermission")
    fun BluetoothManager.startDiscovery() = try {
        adapter.startDiscovery()
    } catch (e: Throwable) {
        Log.d(TAG, "startDiscovery: ${e.message}")
        false
    }

    @SuppressLint("MissingPermission")
    fun BluetoothManager.cancelDiscoverySafely() {
        try {
            adapter.cancelDiscovery()
        } catch (e: Throwable) {
            Log.d(TAG, "cancelDiscovery: ${e.message}")
        }
    }

    val BluetoothManager.bondedDevices: Set<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get() = try {
            adapter.bondedDevices
        } catch (e: Throwable) {
            Log.d(TAG, "bondedDevices: ${e.message}")
            emptySet<BluetoothDevice>()
        }

    val BluetoothDevice.deviceName: String? get() = name

    val BluetoothDevice.isBonded
        @SuppressLint("MissingPermission")
        get() = try {
            bondState == BluetoothDevice.BOND_BONDED
        } catch (e: Throwable) {
            Log.d(TAG, "isBonded: ${e.message}")
            false
        }

    fun BluetoothDevice.createSocketAndConnect(): BluetoothSocket {
        val socket = createRfcommSocketToServiceRecord(UUID_SECURE)
        socket.connect()
        return socket
    }

    @SuppressLint("MissingPermission")
    fun BluetoothDevice.createBondSafely(): Boolean {
        try {
            return createBond()
        } catch (e: Throwable) {
            Log.d(TAG, "createBond: ${e.message}")
            return false
        }
    }
}
