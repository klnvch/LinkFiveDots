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

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";

    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;

    private final BluetoothService mBluetoothService;

    ConnectedThread(BluetoothService mBluetoothService, BluetoothSocket socket) {
        Log.d(TAG, "create ConnectedThread");
        this.mBluetoothService = mBluetoothService;

        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    public void run() {
        Log.i(TAG, "run");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                mBluetoothService.sendMessage(BluetoothActivity.MESSAGE_READ, bytes, -1, buffer);
            } catch (IOException e) {
                Log.e(TAG, "run: " + e.getMessage());
                mBluetoothService.connectionLost();
                // Start the service over to restart listening mode
                mBluetoothService.start();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    void write(byte[] buffer) {
        try {
            mOutStream.write(buffer);

            // Share the sent message back to the UI Activity
            mBluetoothService.sendMessage(BluetoothActivity.MESSAGE_WRITE, -1, -1, buffer);
        } catch (IOException e) {
            Log.e(TAG, "write: " + e.getMessage());
        }
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: " + e.getMessage());
        }
    }
}