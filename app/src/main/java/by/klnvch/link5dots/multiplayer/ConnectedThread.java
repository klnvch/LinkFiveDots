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

package by.klnvch.link5dots.multiplayer;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;

import by.klnvch.link5dots.multiplayer.bluetooth.BluetoothService;
import by.klnvch.link5dots.multiplayer.nsd.NsdService;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";

    private final Object mSocket;
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;

    private final MultiplayerService mService;

    public ConnectedThread(@NonNull NsdService service, @NonNull Socket socket) {
        Log.d(TAG, "create ConnectedThread");
        this.mService = service;
        this.mSocket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        this.mInputStream = tmpIn;
        this.mOutputStream = tmpOut;
    }

    public ConnectedThread(@NonNull BluetoothService service, @NonNull BluetoothSocket socket) {
        Log.d(TAG, "create ConnectedThread");
        this.mService = service;
        this.mSocket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        this.mInputStream = tmpIn;
        this.mOutputStream = tmpOut;
    }

    public void run() {
        Log.i(TAG, "run");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mInputStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                if (bytes == -1) {
                    Log.e(TAG, "InputStream read -1");
                    mService.connectionLost();
                    break;
                } else {
                    mService.sendMessage(MultiplayerActivity.MESSAGE_READ, bytes, -1, buffer);
                }
            } catch (IOException e) {
                Log.e(TAG, "InputStream read: " + e.getMessage());
                mService.connectionLost();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(@NonNull byte[] buffer) {
        // NetworkOnMainThreadException - StrictMode$AndroidBlockGuardPolicy.onNetwork
        new WriterTask(this).execute(buffer);
    }

    public void cancel() {
        try {
            if (mSocket instanceof BluetoothSocket) {
                ((BluetoothSocket) mSocket).close();
            } else if (mSocket instanceof Socket) {
                ((Socket) mSocket).close();
            } else {
                Log.e(TAG, "unknown socket");
            }
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }

    private static class WriterTask extends AsyncTask<byte[], Void, Boolean> {
        private final WeakReference<ConnectedThread> mThreadRef;

        private WriterTask(@NonNull ConnectedThread thread) {
            this.mThreadRef = new WeakReference<>(thread);
        }

        @Override
        protected Boolean doInBackground(byte[]... params) {
            ConnectedThread thread = mThreadRef.get();
            if (thread != null) {
                try {
                    thread.mOutputStream.write(params[0]);
                    thread.mService.sendMessage(MultiplayerActivity.MESSAGE_WRITE,
                            -1, -1, params[0]);
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}