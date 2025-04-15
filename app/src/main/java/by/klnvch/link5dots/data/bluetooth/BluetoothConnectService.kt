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
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.UUID_SECURE
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BluetoothConnectService @Inject constructor(private val context: Context) {
    suspend fun connect(device: BluetoothDevice): BluetoothSocket {
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            bond(device)
        }
        val socket = device.createRfcommSocketToServiceRecord(UUID_SECURE)
        socket.connect()
        return socket
    }

    private suspend fun bond(device: BluetoothDevice) =
        suspendCancellableCoroutine { continuation ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    Log.d(TAG, "action: $action")
                    when (action) {
                        BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                            val intentDevice =
                                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                            Log.d(TAG, "bond state: $bondState")
                            if (device.address == intentDevice?.address) {
                                when (bondState) {
                                    BluetoothDevice.BOND_NONE -> {
                                        continuation.resumeWithException(BluetoothBondException())
                                        context.unregisterReceiver(this)
                                    }

                                    BluetoothDevice.BOND_BONDED -> {
                                        continuation.resume(Unit)
                                        context.unregisterReceiver(this)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            context.registerReceiver(
                receiver,
                IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            )
            val result = device.createBond()
            if (!result) {
                context.unregisterReceiver(receiver)
                continuation.resumeWithException(BluetoothBondException())
            }

            continuation.invokeOnCancellation { context.unregisterReceiver(receiver) }
        }
}

class BluetoothBondException : Exception()
