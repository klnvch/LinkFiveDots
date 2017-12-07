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
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import by.klnvch.link5dots.multiplayer.AcceptThread;

/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
class BluetoothAcceptThread
        extends AcceptThread<BluetoothService, BluetoothServerSocket, BluetoothSocket> {

    BluetoothAcceptThread(@NonNull BluetoothService mService) {
        super(mService);
    }

    @Nullable
    @Override
    protected BluetoothServerSocket createSocket() throws IOException {
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        return mAdapter.listenUsingRfcommWithServiceRecord(BluetoothCredentials.NAME_SECURE,
                BluetoothCredentials.UUID_SECURE);
    }

    @Nullable
    @Override
    protected BluetoothSocket acceptSocket() throws Exception {
        return mServerSocket.accept();
    }

    @Override
    protected void confirmConnection() {
        mService.connected(mSocket, mSocket.getRemoteDevice());
    }

    @Override
    protected void closeServerSocket() throws IOException {
        mServerSocket.close();
    }

    @Override
    protected void closeSocket() throws IOException {
        mSocket.close();
    }
}