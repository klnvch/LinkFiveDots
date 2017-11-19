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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.MultiplayerService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdService extends MultiplayerService {

    public static final String BLUETOOTH_GAME_VIEW_PREFERENCES = "BLUETOOTH_GAME_VIEW_PREFERENCES";
    // Constants that indicate the current connection state
    public static final int STATE_UNREGISTERED = 100;
    public static final int STATE_REGISTERING = 101;
    public static final int STATE_REGISTERED = 102;
    public static final int STATE_UNREGISTERING = 103;
    public static final int STATE_DISCOVERING = 200;
    public static final int STATE_IDLE = 201;
    private static final String TAG = "NsdService";
    // new constants and fields
    private static final String SERVICE_NAME = "Link Five Dots";
    private static final String SERVICE_TYPE = "_http._tcp.";
    private final Map<String, NsdServiceInfo> mServices = new HashMap<>();
    // Member fields
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private boolean mResolveListenerInUse = false;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mRegistrationNsdServiceInfo = null;
    private NsdServiceInfo mConnectedNsdServiceInfo = null;
    private NsdManager mNsdManager;
    private int mPort = -1;
    private int mServerState = STATE_UNREGISTERED;
    private int mClientState = STATE_IDLE;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {

        // NoClassDefFoundError (@by.klnvch.link5dots.nsd_picker.NsdService:<init>:294) {main}
        try {
            Class.forName("android.net.nsd.NsdManager");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
            stopSelf();
            return;
        }

        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                setServerState(STATE_UNREGISTERED);
                mRegistrationNsdServiceInfo = null;
                Log.d(TAG, "onRegistrationFailed: " + i);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                setServerState(STATE_UNREGISTERED);
                mRegistrationNsdServiceInfo = null;
                Log.d(TAG, "onUnRegistrationFailed: " + i);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                setServerState(STATE_REGISTERED);
                mRegistrationNsdServiceInfo = nsdServiceInfo;
                Log.d(TAG, "onServiceRegistered: " + nsdServiceInfo);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                setServerState(STATE_UNREGISTERED);
                mRegistrationNsdServiceInfo = null;
                Log.d(TAG, "onServiceUnregistered");
            }
        };
        //
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                Log.e(TAG, "Discovery failed: Error code:" + i);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                Log.e(TAG, "Discovery failed: Error code:" + i);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String s) {
                Log.d(TAG, "onDiscoveryStarted");
            }

            @Override
            public void onDiscoveryStopped(String s) {
                Log.i(TAG, "Discovery stopped: " + s);
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                Log.d(TAG, "Service discovery success" + nsdServiceInfo);
                if (!nsdServiceInfo.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + nsdServiceInfo.getServiceType());
                } else if (mRegistrationNsdServiceInfo != null && nsdServiceInfo.getServiceName()
                        .equals(mRegistrationNsdServiceInfo.getServiceName())) {
                    Log.d(TAG, "Same machine: " + mRegistrationNsdServiceInfo.getServiceName());
                } else if (nsdServiceInfo.getServiceName().contains(SERVICE_NAME)) {
                    if (!mResolveListenerInUse) {
                        mResolveListenerInUse = true;
                        mNsdManager.resolveService(nsdServiceInfo, mResolveListener);
                    }
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Log.e(TAG, "service lost" + nsdServiceInfo);
                mServices.remove(nsdServiceInfo.getServiceName());
                //
                mHandler.obtainMessage(NsdPickerActivity.MESSAGE_SERVICES_LIST_UPDATED).sendToTarget();
            }
        };
        //
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Log.e(TAG, "Resolve failed" + i);
                //
                mResolveListenerInUse = false;
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + nsdServiceInfo);

                if (mRegistrationNsdServiceInfo != null && nsdServiceInfo.getServiceName()
                        .equals(mRegistrationNsdServiceInfo.getServiceName())) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mServices.put(nsdServiceInfo.getServiceName(), nsdServiceInfo);
                //
                mHandler.obtainMessage(NsdPickerActivity.MESSAGE_SERVICES_LIST_UPDATED).sendToTarget();
                //
                mResolveListenerInUse = false;
            }
        };
        //
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        //
        mAcceptThread = new AcceptThread(this);
        mAcceptThread.start();
        //
        mState = STATE_NONE;
        start();
    }

    @Override
    public void onDestroy() {
        stop();
        if (mServerState == STATE_REGISTERED) {
            mNsdManager.unregisterService(mRegistrationListener);
        }
        if (mClientState == STATE_DISCOVERING) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
        //
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        if (mHandler != null) {
            mHandler.obtainMessage(NsdActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Override
    public synchronized void start() {
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
        setState(STATE_LISTEN);
    }

    public synchronized void connect(NsdServiceInfo serviceInfo) {

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
        mConnectThread = new ConnectThread(this, serviceInfo);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    synchronized void connected(Socket socket, NsdServiceInfo nsdServiceInfo) {
        this.mConnectedNsdServiceInfo = nsdServiceInfo;

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

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(this, socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        mHandler.obtainMessage(NsdPickerActivity.MESSAGE_DEVICE_NAME).sendToTarget();

        setState(STATE_CONNECTED);

        // clean preferences
        SharedPreferences prefs = getSharedPreferences(BLUETOOTH_GAME_VIEW_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Stop all threads
     */
    @Override
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an not synchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    @Override
    public void write(@NonNull byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write not synchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    public void connectionFailed(NsdServiceInfo nsdServiceInfo) {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(NsdActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(NsdPickerActivity.TOAST, R.string.bluetooth_connecting_error_message);
        bundle.putString(NsdPickerActivity.DEVICE_NAME, nsdServiceInfo.getServiceName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        NsdService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    public void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(NsdActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putInt(NsdActivity.TOAST, R.string.bluetooth_disconnected);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        NsdService.this.start();
    }

    public void sendMessage(int what, int arg1, int arg2, Object obj) {
        mHandler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
    }

    public void setLocalPort(int port) {
        this.mPort = port;
    }

    public String getServiceName() {
        return mRegistrationNsdServiceInfo.getServiceName();
    }

    public NsdServiceInfo getRegistrationNsdServiceInfo() {
        return this.mRegistrationNsdServiceInfo;
    }

    // new functions

    @NonNull
    @Override
    public String getDestinationName() {
        return mConnectedNsdServiceInfo.getServiceName();
    }

    public Collection<NsdServiceInfo> getServices() {
        return mServices.values();
    }

    public int getServerState() {
        return mServerState;
    }

    private synchronized void setServerState(int state) {
        mServerState = state;

        if (mHandler != null) {
            mHandler.obtainMessage(NsdPickerActivity.MESSAGE_SERVER_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    public int getClientState() {
        return mClientState;
    }

    private synchronized void setClientState(int state) {
        mClientState = state;

        if (mHandler != null) {
            mHandler.obtainMessage(NsdPickerActivity.MESSAGE_CLIENT_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    public void registerService() {
        if (mPort > -1) {
            setServerState(STATE_REGISTERING);

            NsdServiceInfo serviceInfo = new NsdServiceInfo();
            serviceInfo.setPort(mPort);
            serviceInfo.setServiceName(SERVICE_NAME);
            serviceInfo.setServiceType(SERVICE_TYPE);

            mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        } else {
            Log.i(TAG, "ServerSocket isn't bound");
        }
    }

    public void unRegisterService() {
        setServerState(STATE_UNREGISTERING);
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public void discoverServices() {
        setClientState(STATE_DISCOVERING);
        mServices.clear();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        setClientState(STATE_IDLE);
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }
}