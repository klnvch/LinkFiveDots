/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
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

package by.klnvch.link5dots.multiplayer.utils.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.fragment.app.Fragment
import java.io.IOException
import java.util.*

object BluetoothHelper {

    private const val TAG = "BluetoothHelper"
    private const val NAME_SECURE = "BluetoothLinkFiveDotsSecure"
    private val UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    private const val FAKE_ADDRESS = "02:00:00:00:00:00"
    private const val DISCOVERABLE_DURATION_SECONDS = 30

    val isSupported: Boolean
        get() = getAdapter() != null

    val isEnabled: Boolean
        get() = getAdapter()!!.isEnabled

    val bondedDevices: Set<BluetoothDevice>
        get() = getAdapter()!!.bondedDevices

    val name: String
        get() = getAdapter()!!.name

    val address: String?
        @SuppressLint("HardwareIds")
        get() {
            val address = getAdapter()?.address
            return if (FAKE_ADDRESS == address) {
                null
            } else {
                address
            }
        }

    fun disable() {
        getAdapter()?.disable()
    }

    fun requestEnable(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), requestCode)
    }

    fun requestDiscoverable(fragment: Fragment, requestCode: Int) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                .putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        DISCOVERABLE_DURATION_SECONDS)
        fragment.startActivityForResult(intent, requestCode)
    }

    fun registerReceiverAndStartDiscovery(context: Context, receiver: BroadcastReceiver) {
        context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        startDiscovery()
    }

    fun unregisterReceiverAndCancelDiscovery(context: Context, receiver: BroadcastReceiver) {
        cancelDiscovery()
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "unregisterReceiver: " + e.message)
        }

    }

    fun startDiscovery() {
        getAdapter()?.startDiscovery()
    }

    fun cancelDiscovery() {
        getAdapter()?.cancelDiscovery()
    }

    fun isDeviceFound(intent: Intent): BluetoothDevice? {
        if (BluetoothDevice.ACTION_FOUND == intent.action) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                return device
            }
        }
        return null
    }

    fun isDiscoveryFinished(intent: Intent): Boolean {
        return BluetoothAdapter.ACTION_DISCOVERY_FINISHED == intent.action
    }

    @Throws(IOException::class)
    fun createServerSocket(): BluetoothServerSocket {
        return getAdapter()!!.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE)
    }

    @Throws(IOException::class)
    fun createSocket(device: BluetoothDevice): BluetoothSocket {
        return device.createRfcommSocketToServiceRecord(UUID_SECURE)
    }

    private fun getAdapter(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter()
    }
}
