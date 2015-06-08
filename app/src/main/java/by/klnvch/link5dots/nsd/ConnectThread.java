package by.klnvch.link5dots.nsd;

import android.annotation.TargetApi;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class ConnectThread extends Thread{
    private static final String TAG = "ConnectThread";

    private final Socket mSocket;

    private final NsdService mNsdService;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ConnectThread(NsdService mNsdService, NsdServiceInfo serviceInfo) {
        this.mNsdService = mNsdService;

        Socket tmp = null;

        try {
            tmp = new Socket(serviceInfo.getHost(), serviceInfo.getPort());
        } catch (IOException e) {
            Log.e(TAG, "create() failed", e);
        }
        mSocket = tmp;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectThread");
        setName(TAG);

        // Reset the ConnectThread because we're done
        synchronized (mNsdService) {
            mNsdService.resetConnectThread();
        }

        // Start the connected thread
        mNsdService.connected(mSocket);
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
