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

public abstract class ConnectThread<Service extends MultiplayerService, Destination, Socket>
        extends Thread {
    protected static final String TAG = "ConnectThread";

    protected final Service mService;
    protected final Destination mDestination;
    protected Socket mSocket;

    protected ConnectThread(@NonNull Service service, @NonNull Destination destination) {
        this.mService = service;
        this.mDestination = destination;
        setName(TAG);
    }

    @Override
    public void run() {
        Log.d(TAG, "run");

        try {
            mSocket = createSocket();
        } catch (IOException e) {
            Log.e(TAG, "socket creation failed", e);
            cancel();
        }

        if (mSocket != null) {
            confirmConnection();
        } else {
            mService.connectionFailed();
        }
    }

    @Nullable
    protected abstract Socket createSocket() throws IOException;

    protected abstract void closeSocket() throws IOException;

    protected abstract void confirmConnection();

    public void cancel() {
        if (mSocket != null) {
            try {
                closeSocket();
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }
            mSocket = null;
        }
    }
}
