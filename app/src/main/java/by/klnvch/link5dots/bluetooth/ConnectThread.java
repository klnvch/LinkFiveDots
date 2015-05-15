package by.klnvch.link5dots.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
class ConnectThread extends Thread{
    private static final String TAG = "ConnectThread";

    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    private final BluetoothService mBluetoothService;

    public ConnectThread(BluetoothService mBluetoothService, BluetoothDevice device) {
        this.mBluetoothService = mBluetoothService;

        mmDevice = device;
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(BluetoothService.UUID_SECURE);
        } catch (IOException e) {
            Log.e(TAG, "create() failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectThread");
        setName(TAG);

        // Always cancel discovery because it will slow down a connection
        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket.connect();
        } catch (IOException e) {
            // Close the socket
            try {
                mmSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }
            mBluetoothService.connectionFailed(mmDevice);
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (mBluetoothService) {
            mBluetoothService.resetConnectThread();
        }

        // Start the connected thread
        mBluetoothService.connected(mmSocket, mmDevice);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
