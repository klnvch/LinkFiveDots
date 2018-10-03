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

package by.klnvch.link5dots.multiplayer.threads;

import android.net.TrafficStats;
import android.util.Log;

import java.io.IOException;

import androidx.annotation.NonNull;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private static final int THREAD_ID = 2;
    private final OnSocketConnectedListener mConnectedListener;
    private final SocketDecorator.Builder mSocketBuilder;
    private SocketDecorator mSocket;

    public ConnectThread(@NonNull OnSocketConnectedListener connectedListener,
                         @NonNull SocketDecorator.Builder socketBuilder) {
        setName(TAG);

        this.mConnectedListener = connectedListener;
        this.mSocketBuilder = socketBuilder;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: started");

        Exception exception = null;

        try {
            TrafficStats.setThreadStatsTag(THREAD_ID);
            mSocket = mSocketBuilder.build();
        } catch (IOException e) {
            Log.e(TAG, "run: ", e);
            exception = e;
        }

        if (exception == null)
            mConnectedListener.onSocketConnected(mSocket);
        else if (!isInterrupted())
            mConnectedListener.onSocketFailed(exception);

        Log.d(TAG, "run: finished");
    }

    @Override
    public void interrupt() {
        super.interrupt();

        if (mSocket != null)
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            } finally {
                mSocket = null;
            }
    }
}