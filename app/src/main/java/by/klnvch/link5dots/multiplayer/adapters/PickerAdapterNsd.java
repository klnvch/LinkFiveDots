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

package by.klnvch.link5dots.multiplayer.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import by.klnvch.link5dots.multiplayer.targets.TargetNsd;
import by.klnvch.link5dots.multiplayer.utils.nsd.NsdCredentials;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class PickerAdapterNsd extends PickerAdapterSockets implements NsdManager.DiscoveryListener {

    private static final String TAG = "NsdPickerAdapter";

    private final NsdManager mNsdManager;

    public PickerAdapterNsd(@NonNull Context context) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    @Override
    protected void startListening() {
        mNsdManager.discoverServices(NsdCredentials.SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, this);
    }

    @Override
    protected void stopListening() {
        try {
            mNsdManager.stopServiceDiscovery(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "stopListening: " + e.getMessage());
        } finally {
            new Handler(Looper.getMainLooper()).post(this::clear);
        }
    }

    private synchronized void resolveService(@NonNull NsdServiceInfo serviceInfo) {
        try {
            mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e(TAG, "onResolveFailed: " + errorCode);
                }

                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.d(TAG, "onServiceResolved: " + serviceInfo);

                    new Handler(Looper.getMainLooper()).post(() -> add(new TargetNsd(serviceInfo)));
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "resolveService: " + e.getMessage());
        }
    }

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        if (mOnScanStoppedListener != null) {
            mOnScanStoppedListener.onScanStopped(new Exception("onStartDiscoveryFailed: " + errorCode));
        }
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.e(TAG, "onStopDiscoveryFailed: " + errorCode);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d(TAG, "onDiscoveryStarted");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "onDiscoveryStopped");
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "onServiceFound: " + serviceInfo);

        if (!serviceInfo.getServiceType().equals(NsdCredentials.SERVICE_TYPE)) {
            Log.e(TAG, "Unknown Service Type: " + serviceInfo.getServiceType());
        } else if (serviceInfo.getServiceName().contains(NsdCredentials.SERVICE_NAME)) {
            resolveService(serviceInfo);
        } else {
            Log.e(TAG, "unknown case: " + serviceInfo);
        }
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.e(TAG, "onServiceLost: " + serviceInfo);

        new Handler(Looper.getMainLooper()).post(() -> remove(new TargetNsd(serviceInfo)));
    }
}