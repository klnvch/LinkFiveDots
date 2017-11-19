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

import android.annotation.TargetApi;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private final NsdService mNsdService;
    private final NsdServiceInfo mNsdServiceInfo;
    private Socket mSocket;

    ConnectThread(NsdService mNsdService, NsdServiceInfo mNsdServiceInfo) {
        this.mNsdService = mNsdService;
        this.mNsdServiceInfo = mNsdServiceInfo;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectThread");
        setName(TAG);

        try {
            mSocket = new Socket(mNsdServiceInfo.getHost(), mNsdServiceInfo.getPort());
        } catch (IOException e) {
            Log.e(TAG, "socket creation failed: " + e.getMessage());
        }

        if (mSocket != null) {
            mNsdService.connected(mSocket, mNsdServiceInfo);
        } else {
            mNsdService.connectionFailed(mNsdServiceInfo);
        }
    }

    public void cancel() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "closing socket failed " + e.getMessage());
            }
        }
    }
}