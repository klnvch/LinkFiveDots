package by.klnvch.link5dots.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";

    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;

    private final BluetoothService mBluetoothService;

    public ConnectedThread(BluetoothService mBluetoothService, BluetoothSocket socket) {
        Log.d(TAG, "create ConnectedThread");
        this.mBluetoothService = mBluetoothService;

        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    public void run() {
        Log.i(TAG, "run");
        byte[] buffer = new byte[1024];
        int bytes;

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                // Read from the InputStream
                bytes = mInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
                mBluetoothService.sendMessage(BluetoothActivity.MESSAGE_READ, bytes, -1, buffer);
            } catch (IOException e) {
                Log.e(TAG, "run: " + e.getMessage());
                mBluetoothService.connectionLost();
                // Start the service over to restart listening mode
                mBluetoothService.start();
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
            mOutStream.write(buffer);

            // Share the sent message back to the UI Activity
            mBluetoothService.sendMessage(BluetoothActivity.MESSAGE_WRITE, -1, -1, buffer);
        } catch (IOException e) {
            Log.e(TAG, "write: " + e.getMessage());
        }
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "cancel: " + e.getMessage());
        }
    }
}