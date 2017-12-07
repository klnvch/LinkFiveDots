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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public abstract class AcceptThread<Service extends MultiplayerService, ServerSocket, Socket>
        extends Thread {
    protected static final String TAG = "AcceptThread";
    protected final Service mService;
    protected ServerSocket mServerSocket = null;
    protected Socket mSocket = null;

    public AcceptThread(@NonNull Service mService) {
        this.mService = mService;
        setName(TAG);
    }

    @Override
    public void run() {
        Log.d(TAG, "run");

        try {
            mServerSocket = createSocket();
        } catch (IOException e) {
            Log.e(TAG, "ServerSocket creation failed", e);
            return;
        }

        while (mService.getState() != MultiplayerService.STATE_CONNECTED) {
            try {
                mSocket = acceptSocket();
            } catch (Exception e) {
                Log.e(TAG, "accept() failed", e);
                break;
            }

            // If a connection was accepted
            if (mSocket != null) {
                synchronized (mService) {
                    switch (mService.getState()) {
                        case MultiplayerService.STATE_LISTEN:
                        case MultiplayerService.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            confirmConnection();
                            break;
                        case MultiplayerService.STATE_NONE:
                        case MultiplayerService.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                closeSocket();
                            } catch (IOException e) {
                                Log.e(TAG, "close() of socket failed", e);
                            }
                            mSocket = null;
                            break;
                    }
                }
            }
        }
        Log.d(TAG, "accept thread finished");
    }

    @Nullable
    protected abstract ServerSocket createSocket() throws IOException;

    protected abstract Socket acceptSocket() throws Exception;

    protected abstract void confirmConnection();

    protected abstract void closeServerSocket() throws IOException;

    protected abstract void closeSocket() throws IOException;

    public void cancel() {
        if (mServerSocket != null) {
            try {
                closeServerSocket();
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }
            mServerSocket = null;
        }
    }
}