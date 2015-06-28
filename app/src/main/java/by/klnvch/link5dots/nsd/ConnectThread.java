package by.klnvch.link5dots.nsd;

import android.annotation.TargetApi;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ConnectThread extends Thread{
    private static final String TAG = "ConnectThread";

    private Socket mSocket;

    private final NsdService mNsdService;
    private final NsdServiceInfo mNsdServiceInfo;

    public ConnectThread(NsdService mNsdService, NsdServiceInfo mNsdServiceInfo) {
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
