package by.klnvch.link5dots.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * This thread runs while listening for incoming connections. It behaves
 * like a server-side client. It runs until a connection is accepted
 * (or until cancelled).
 */
class AcceptThread extends Thread {
    private static final String TAG = "AcceptThread";
    // The local server socket
    private final BluetoothServerSocket mServerSocket;
    private final BluetoothService mBluetoothService;

    public AcceptThread(BluetoothService mBluetoothService) {
        this.mBluetoothService = mBluetoothService;
        BluetoothServerSocket tmp = null;

        // Create a new listening server socket
        try {
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            tmp = mAdapter.listenUsingRfcommWithServiceRecord(BluetoothService.NAME_SECURE, BluetoothService.UUID_SECURE);
        } catch (IOException e) {
            Log.e(TAG, "constructor: " + e.getMessage());
        }
        mServerSocket = tmp;
    }

    public void run() {
        setName(TAG);

        BluetoothSocket socket;

        // Listen to the server socket if we're not connected
        while (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mServerSocket.accept();
            } catch (Exception e) {
                Log.e(TAG, "run: " + e.getMessage());
                break;
            }

            // If a connection was accepted
            if (socket != null) {
                synchronized (mBluetoothService) {
                    switch (mBluetoothService.getState()) {
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            mBluetoothService.connected(socket, socket.getRemoteDevice());
                            break;
                        case BluetoothService.STATE_NONE:
                        case BluetoothService.STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "run: " + e.getMessage());
                            }
                            break;
                    }
                }
            }
        }
    }

    public void cancel() {
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "cancel: " + e.getMessage());
        }
    }
}