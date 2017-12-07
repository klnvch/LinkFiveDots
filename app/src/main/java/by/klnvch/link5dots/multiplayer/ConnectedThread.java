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

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public abstract class ConnectedThread<Socket> extends Thread {
    private static final String TAG = "ConnectedThread";

    protected final Socket mSocket;
    private final MultiplayerService mService;
    private final byte[] buffer = new byte[1024];
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public ConnectedThread(@NonNull MultiplayerService service, @NonNull Socket socket) {
        this.mService = service;
        this.mSocket = socket;
    }

    @Override
    public void run() {
        Log.i(TAG, "run");

        try {
            mInputStream = getInputStream();
            mOutputStream = getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                int byteCount = mInputStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                if (byteCount == -1) {
                    Log.e(TAG, "InputStream read -1");
                    mService.connectionLost();
                    break;
                } else {
                    String msg = new String(buffer, 0, byteCount);
                    mService.sendMsg(MultiplayerActivity.MESSAGE_READ, msg);
                }
            } catch (IOException e) {
                Log.e(TAG, "InputStream read: " + e.getMessage());
                mService.connectionLost();
                break;
            }
        }
    }

    @NonNull
    protected abstract InputStream getInputStream() throws IOException;

    @NonNull
    protected abstract OutputStream getOutputStream() throws IOException;

    protected abstract void closeSocket() throws IOException;

    /**
     * Write to the connected OutStream.
     *
     * @param msg The bytes to write
     */
    void write(@NonNull String msg) {
        // NetworkOnMainThreadException - StrictMode$AndroidBlockGuardPolicy.onNetwork
        new WriterTask(this).execute(msg);
    }

    public void cancel() {
        try {
            closeSocket();
        } catch (IOException e) {
            Log.e(TAG, "close() of socket failed", e);
        }
    }

    private static class WriterTask extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<ConnectedThread> mThreadRef;

        private WriterTask(@NonNull ConnectedThread thread) {
            this.mThreadRef = new WeakReference<>(thread);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            ConnectedThread thread = mThreadRef.get();
            if (thread != null) {
                try {
                    thread.mOutputStream.write(params[0].getBytes());
                    thread.mService.sendMsg(MultiplayerActivity.MESSAGE_WRITE, params[0]);
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