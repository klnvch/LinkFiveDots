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
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import by.klnvch.link5dots.multiplayer.AcceptThread;
import by.klnvch.link5dots.multiplayer.ConnectThread;
import by.klnvch.link5dots.multiplayer.ConnectedThread;
import by.klnvch.link5dots.multiplayer.MultiplayerService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdService extends MultiplayerService<Socket, NsdServiceInfo> {

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
    private boolean mResolveListenerInUse = false;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mLocalNsdInfo = null;
    private NsdManager mNsdManager;
    private int mPort = -1;
    private int mServerState = STATE_UNREGISTERED;
    private int mClientState = STATE_IDLE;

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
                mLocalNsdInfo = null;
                Log.d(TAG, "onRegistrationFailed: " + i);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {
                setServerState(STATE_UNREGISTERED);
                mLocalNsdInfo = null;
                Log.d(TAG, "onUnRegistrationFailed: " + i);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                setServerState(STATE_REGISTERED);
                mLocalNsdInfo = nsdServiceInfo;
                Log.d(TAG, "onServiceRegistered: " + nsdServiceInfo);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                setServerState(STATE_UNREGISTERED);
                mLocalNsdInfo = null;
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
                } else if (mLocalNsdInfo != null && nsdServiceInfo.getServiceName()
                        .equals(mLocalNsdInfo.getServiceName())) {
                    Log.d(TAG, "Same machine: " + mLocalNsdInfo.getServiceName());
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
                sendMsg(NsdPickerActivity.MESSAGE_SERVICES_LIST_UPDATED);
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

                if (mLocalNsdInfo != null && nsdServiceInfo.getServiceName()
                        .equals(mLocalNsdInfo.getServiceName())) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mServices.put(nsdServiceInfo.getServiceName(), nsdServiceInfo);
                //
                sendMsg(NsdPickerActivity.MESSAGE_SERVICES_LIST_UPDATED);
                //
                mResolveListenerInUse = false;
            }
        };
        //
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        //
        mState = STATE_NONE;
        start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy(); // important

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

    @NonNull
    @Override
    protected AcceptThread createAcceptThread() {
        return new NsdAcceptThread(this);
    }

    @NonNull
    @Override
    protected ConnectThread createConnectThread(@NonNull NsdServiceInfo nsdServiceInfo) {
        return new NsdConnectThread(this, nsdServiceInfo);
    }

    @NonNull
    @Override
    protected ConnectedThread createConnectedThread(@NonNull Socket socket) {
        return new NsdConnectedThread(this, socket);
    }

    public void setLocalPort(int port) {
        this.mPort = port;
    }

    public String getServiceName() {
        return mLocalNsdInfo.getServiceName();
    }

    public NsdServiceInfo getRegistrationNsdServiceInfo() {
        return this.mLocalNsdInfo;
    }

    // new functions

    @NonNull
    @Override
    public String getDestinationName() {
        if (mDestination != null) {
            return mDestination.getServiceName();
        } else {
            return "";
        }
    }

    public Collection<NsdServiceInfo> getServices() {
        return mServices.values();
    }

    public int getServerState() {
        return mServerState;
    }

    private synchronized void setServerState(int state) {
        mServerState = state;
        sendMsg(NsdPickerActivity.MESSAGE_SERVER_STATE_CHANGE, state);
    }

    public int getClientState() {
        return mClientState;
    }

    private synchronized void setClientState(int state) {
        mClientState = state;
        sendMsg(NsdPickerActivity.MESSAGE_SERVER_STATE_CHANGE, state);
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