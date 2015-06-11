package by.klnvch.link5dots.nsd;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class AcceptThread extends Thread{
    private static final String TAG = "AcceptThread";

    private final ServerSocket mServerSocket;
    private final NsdService mNsdService;

    public AcceptThread(NsdService mNsdService) {
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

        while (mNsdService.getState() != NsdService.STATE_CONNECTED) {
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
                            mNsdService.connected(socket);
                            break;
                        case NsdService.STATE_NONE:
                        case NsdService.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                    }
                }
            }
        }
    }
    public void cancel() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of server failed", e);
        }
    }
}
