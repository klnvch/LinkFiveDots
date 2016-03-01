package by.klnvch.link5dots.nsd;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";

    private final Socket mSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private final NsdService mNsdService;

    public ConnectedThread(NsdService mNsdService, Socket socket) {
        Log.d(TAG, "create ConnectedThread");
        this.mNsdService = mNsdService;

        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                if (bytes == -1) {
                    Log.e(TAG, "InputStream read -1");
                    mNsdService.connectionLost();
                    break;
                } else {
                    mNsdService.sendMessage(NsdActivity.MESSAGE_READ, bytes, -1, buffer);
                }
            } catch (IOException e) {
                Log.e(TAG, "InputStream read: " + e.getMessage());
                mNsdService.connectionLost();
                break;
            }
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);

            // Share the sent message back to the UI Activity
            mNsdService.sendMessage(NsdActivity.MESSAGE_WRITE, -1, -1, buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
