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

package by.klnvch.link5dots.multiplayer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import by.klnvch.link5dots.multiplayer.AcceptThread;
import by.klnvch.link5dots.multiplayer.ConnectThread;
import by.klnvch.link5dots.multiplayer.ConnectedThread;
import by.klnvch.link5dots.multiplayer.MultiplayerService;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService extends MultiplayerService<BluetoothSocket, BluetoothDevice> {

    private static final String TAG = "BluetoothService";

    @Override
    public void onCreate() {

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Log.e(TAG, "bluetooth adapter is null");
            stopSelf();
            return;
        }

        mState = STATE_NONE;
        start();
    }

    @NonNull
    @Override
    protected AcceptThread createAcceptThread() {
        return new BluetoothAcceptThread(this);
    }

    @NonNull
    @Override
    protected ConnectThread createConnectThread(@NonNull BluetoothDevice bluetoothDevice) {
        return new BluetoothConnectThread(this, bluetoothDevice);
    }

    @NonNull
    @Override
    protected ConnectedThread createConnectedThread(@NonNull BluetoothSocket bluetoothSocket) {
        return new BluetoothConnectedThread(this, bluetoothSocket);
    }

    @NonNull
    @Override
    public String getDestinationName() {
        if (mDestination != null) {
            return mDestination.getName();
        } else {
            return "";
        }
    }
}