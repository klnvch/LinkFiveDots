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
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;

public class AcceptThread extends Thread {
    private static final String TAG = "AcceptThread";
    private static final int THREAD_ID = 1;
    private final OnSocketConnectedListener mConnectedListener;
    private final ServerSocketDecorator mServerSocket;
    private SocketDecorator mSocket = null;

    public AcceptThread(@NonNull OnSocketConnectedListener connectedListener,
                        @NonNull ServerSocketDecorator serverSocket) {
        setName(TAG);

        this.mConnectedListener = connectedListener;
        this.mServerSocket = serverSocket;
    }

    @Override
    public void run() {
        Log.d(TAG, "run started");

        Exception exception = null;

        Log.d(TAG, "accept started");
        try {
            TrafficStats.setThreadStatsTag(THREAD_ID);
            mSocket = mServerSocket.accept();
        } catch (Exception e) {
            exception = e;
        }
        Log.d(TAG, "accept finished: " + exception);

        if (exception == null)
            if (mSocket != null)
                mConnectedListener.onSocketConnected(mSocket);
            else
                mConnectedListener.onSocketFailed(new Exception("socket is null"));
        else if (!isInterrupted())
            mConnectedListener.onSocketFailed(exception);

        Log.d(TAG, "run finished");
    }

    @Override
    public void interrupt() {
        super.interrupt();

        if (mServerSocket != null)
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }

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