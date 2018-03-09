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

package by.klnvch.link5dots.multiplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.nsd.NsdActivity;
import by.klnvch.link5dots.multiplayer.nsd.NsdPickerActivity;

public abstract class MultiplayerService<Socket, Destination> extends Service {

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    protected int mState = STATE_NONE;
    protected AcceptThread mAcceptThread = null;
    protected Destination mDestination = null;
    private ConnectThread mConnectThread = null;
    private ConnectedThread mConnectedThread = null;
    private Handler mHandler = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stop();
    }

    @NonNull
    protected abstract AcceptThread createAcceptThread();

    @NonNull
    protected abstract ConnectThread createConnectThread(@NonNull Destination destination);

    @NonNull
    protected abstract ConnectedThread createConnectedThread(@NonNull Socket socket);

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        sendMsg(MultiplayerActivity.MESSAGE_STATE_CHANGE, state);
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public void setHandler(@NonNull Handler handler) {
        this.mHandler = handler;
    }

    /**
     * Write to the ConnectedThread in an not synchronized manner
     *
     * @param msg The bytes to write
     * @see ConnectedThread#write(String)
     */
    public void write(@NonNull String msg) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write not synchronized
        r.write(msg);
    }

    @NonNull
    public abstract String getDestinationName();

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = createAcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param destination The BluetoothDevice or NsdServiceInfo to connect
     */
    public synchronized void connect(@NonNull Destination destination) {

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = createConnectThread(destination);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket      The BluetoothSocket on which the connection was made
     * @param destination The BluetoothDevice that has been connected
     */
    public synchronized void connected(@NonNull Socket socket, @NonNull Destination destination) {
        this.mDestination = destination;

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            //mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = createConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        sendMsg(MultiplayerActivity.MESSAGE_DEVICE_NAME);

        setState(STATE_CONNECTED);
    }

    public void sendMsg(int what, String buffer) {
        if (mHandler != null) {
            mHandler.obtainMessage(what, -1, -1, buffer).sendToTarget();
        } else {
            Crashlytics.logException(new Exception("handler is null: " + what));
        }
    }

    public void sendMsg(int what, int state) {
        if (mHandler != null) {
            mHandler.obtainMessage(what, state, -1).sendToTarget();
        } else {
            Crashlytics.logException(new Exception("handler is null: " + what + " and " + state));
        }
    }

    public void sendMsg(int what) {
        if (mHandler != null) {
            mHandler.obtainMessage(what, -1, -1).sendToTarget();
        } else {
            Crashlytics.logException(new Exception("handler is null: " + what));
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    public void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MultiplayerActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(MultiplayerActivity.TOAST, R.string.bluetooth_disconnected);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        start();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    public void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(NsdActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(NsdPickerActivity.TOAST, R.string.bluetooth_connecting_error_message);
        bundle.putString(NsdPickerActivity.DEVICE_NAME, getDestinationName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        start();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public MultiplayerService getService() {
            return MultiplayerService.this;
        }
    }
}