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

package by.klnvch.link5dots.multiplayer.utils.nsd;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdHelper {
    private static final String TAG = "NsdHelper";

    private static final String SERVICE_NAME = "Link Five Dots";
    private static final String SERVICE_TYPE = "_http._tcp.";

    private static NsdManager mNsdManager = null;

    public static void init(@NonNull Context context) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public static void destroy() {
        mNsdManager = null;
    }

    public static boolean isSupported() {
        try {
            Class.forName("android.net.nsd.NsdManager");
            return true;
        } catch (ClassNotFoundException e) {
            Crashlytics.logException(e);
        }
        return false;
    }

    public static void discoverServices(@NonNull NsdManager.DiscoveryListener listener) {
        checkNotNull(mNsdManager);
        checkNotNull(listener);

        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener);
    }

    public static void stopServiceDiscovery(@NonNull NsdManager.DiscoveryListener listener) {
        checkNotNull(mNsdManager);
        checkNotNull(listener);

        try {
            mNsdManager.stopServiceDiscovery(listener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "stopServiceDiscovery: " + e.getMessage());
        }
    }

    public static void resolveService(@NonNull NsdServiceInfo serviceInfo,
                                      @NonNull NsdManager.ResolveListener listener) {
        checkNotNull(mNsdManager);
        checkNotNull(serviceInfo);
        checkNotNull(listener);

        try {
            mNsdManager.resolveService(serviceInfo, listener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "resolveService: " + e.getMessage());
        }
    }

    static void registerService(int port,
                                @NonNull NsdManager.RegistrationListener listener) {
        checkNotNull(mNsdManager);
        checkArgument(port > 0);
        checkNotNull(listener);

        final NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener);
    }

    static void unregisterService(@NonNull NsdManager.RegistrationListener listener) {
        checkNotNull(mNsdManager);
        checkNotNull(listener);

        try {
            mNsdManager.unregisterService(listener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "unregisterService: " + e.getMessage());
        }
    }

    public static boolean isValid(@NonNull NsdServiceInfo serviceInfo) {
        if (!serviceInfo.getServiceType().equals(NsdHelper.SERVICE_TYPE)) {
            Log.e(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
            return false;
        } else if (serviceInfo.getServiceName().contains(NsdHelper.SERVICE_NAME)) {
            return true;
        } else {
            Log.e(TAG, "unknown case: " + serviceInfo);
            return false;
        }
    }

    public static class NsdDiscoveryAdapter implements NsdManager.DiscoveryListener {
        @CallSuper
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "onStartDiscoveryFailed: " + errorCode);
        }

        @CallSuper
        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "onStopDiscoveryFailed: " + errorCode);
        }

        @CallSuper
        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d(TAG, "onDiscoveryStarted");
        }

        @CallSuper
        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.d(TAG, "onDiscoveryStopped");
        }

        @CallSuper
        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "onServiceFound: " + serviceInfo);
        }

        @CallSuper
        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.e(TAG, "onServiceLost: " + serviceInfo);
        }
    }

    public abstract static class NsdResolveAdapter implements NsdManager.ResolveListener {
        @CallSuper
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "onResolveFailed: " + errorCode);
        }

        @CallSuper
        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "onServiceResolved: " + serviceInfo);
        }
    }
}