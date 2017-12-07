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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import by.klnvch.link5dots.multiplayer.ConnectThread;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
class BluetoothConnectThread
        extends ConnectThread<BluetoothService, BluetoothDevice, BluetoothSocket> {

    BluetoothConnectThread(@NonNull BluetoothService service,
                           @NonNull BluetoothDevice destination) {
        super(service, destination);
    }

    @Nullable
    @Override
    protected BluetoothSocket createSocket() throws IOException {
        BluetoothSocket socket = mDestination
                .createRfcommSocketToServiceRecord(BluetoothCredentials.UUID_SECURE);
        socket.connect();
        return socket;
    }

    @Override
    protected void closeSocket() throws IOException {
        mSocket.close();
    }

    @Override
    protected void confirmConnection() {
        mService.connected(mSocket, mDestination);
    }
}