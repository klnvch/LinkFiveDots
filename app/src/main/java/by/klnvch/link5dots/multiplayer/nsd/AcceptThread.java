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

package by.klnvch.link5dots.multiplayer.nsd;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


class AcceptThread extends Thread {
    private static final String TAG = "AcceptThread";

    private final ServerSocket mServerSocket;
    private final NsdService mNsdService;

    AcceptThread(NsdService mNsdService) {
        this.mNsdService = mNsdService;
        ServerSocket tmp = null;

        try {
            tmp = new ServerSocket(0);
            mNsdService.setLocalPort(tmp.getLocalPort());
        } catch (IOException e) {
            Log.e(TAG, "ServerSocket creation failed", e);
        }
        mServerSocket = tmp;
    }

    public void run() {
        setName(TAG);

        Socket socket;

        while (!mServerSocket.isClosed()) {
            try {
                socket = mServerSocket.accept();
            } catch (Exception e) {
                Log.d(TAG, "accept() failed");
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                synchronized (mNsdService) {
                    switch (mNsdService.getState()) {
                        case NsdService.STATE_LISTEN:
                        case NsdService.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            mNsdService.connected(socket, mNsdService.getRegistrationNsdServiceInfo());
                            break;
                        case NsdService.STATE_NONE:
                        case NsdService.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "close failed: " + e.getMessage());
                            }
                            break;
                    }
                }
            }
        }
        Log.d(TAG, "accept thread finished");
    }

    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of server failed", e);
        }
    }
}