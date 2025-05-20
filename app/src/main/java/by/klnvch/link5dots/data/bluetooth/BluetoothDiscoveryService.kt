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

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.IntentCompat
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.cancelDiscoverySafely
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.startDiscovery
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class BluetoothDiscoveryService @Inject constructor(private val context: Context) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)

    fun discover() = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                Log.d(TAG, "action: $action")
                when (action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device =
                            IntentCompat.getParcelableExtra(
                                intent,
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        trySendBlocking(device)
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        channel.close()
                    }
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        val state = bluetoothManager.adapter.state
        val startDiscoveryResult = bluetoothManager.startDiscovery()
        Log.d(TAG, "startDiscovery: state=$state; result=$startDiscoveryResult")
        if (!startDiscoveryResult) {
            channel.close()
        }

        awaitClose {
            bluetoothManager.cancelDiscoverySafely()
            context.unregisterReceiver(receiver)
        }
    }
}
