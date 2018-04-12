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
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import by.klnvch.link5dots.multiplayer.targets.TargetNsd;
import by.klnvch.link5dots.multiplayer.utils.nsd.NsdHelper;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class PickerAdapterNsd extends PickerAdapterSockets {

    private final NsdHelper.NsdDiscoveryAdapter mDiscovery = new NsdHelper.NsdDiscoveryAdapter() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            super.onStartDiscoveryFailed(serviceType, errorCode);

            if (mOnScanStoppedListener != null) {
                mOnScanStoppedListener.onScanStopped(new Exception("onStartDiscoveryFailed: " + errorCode));
            }
            stopScan();
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            super.onServiceFound(serviceInfo);

            if (NsdHelper.isValid(serviceInfo)) {
                resolveService(serviceInfo);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            super.onServiceLost(serviceInfo);

            new Handler(Looper.getMainLooper()).post(() -> remove(new TargetNsd(serviceInfo)));
        }
    };

    @Override
    protected void startListening() {
        NsdHelper.discoverServices(mDiscovery);
    }

    @Override
    protected void stopListening() {
        NsdHelper.stopServiceDiscovery(mDiscovery);

        new Handler(Looper.getMainLooper()).post(this::clear);
    }

    private synchronized void resolveService(@NonNull NsdServiceInfo serviceInfo) {
        NsdHelper.resolveService(serviceInfo, new NsdHelper.NsdResolveAdapter() {
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                super.onServiceResolved(serviceInfo);

                new Handler(Looper.getMainLooper()).post(() -> add(new TargetNsd(serviceInfo)));
            }
        });
    }
}