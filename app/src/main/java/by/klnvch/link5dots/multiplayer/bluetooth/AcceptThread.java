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
import android.util.Log;

import java.io.IOException;

/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
class AcceptThread extends Thread {
    private static final String TAG = "AcceptThread";
    // The local server socket
    private final BluetoothServerSocket mServerSocket;
    private final BluetoothService mBluetoothService;

    AcceptThread(BluetoothService mBluetoothService) {
        this.mBluetoothService = mBluetoothService;
        BluetoothServerSocket tmp = null;

        // Create a new listening server socket
        try {
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            tmp = mAdapter.listenUsingRfcommWithServiceRecord(BluetoothService.NAME_SECURE, BluetoothService.UUID_SECURE);
        } catch (IOException e) {
            Log.e(TAG, "constructor: " + e.getMessage());
        }
        mServerSocket = tmp;
    }

    public void run() {
        setName(TAG);

        BluetoothSocket socket;

        // Listen to the server socket if we're not connected
        while (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mServerSocket.accept();
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                synchronized (mBluetoothService) {
                    switch (mBluetoothService.getState()) {
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            mBluetoothService.connected(socket, socket.getRemoteDevice());
                            break;
                        case BluetoothService.STATE_NONE:
                        case BluetoothService.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "run: " + e.getMessage());
                            }
                            break;
                    }
                }
            }
        }
    }

    public void cancel() {
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "cancel: " + e.getMessage());
        }
    }
}