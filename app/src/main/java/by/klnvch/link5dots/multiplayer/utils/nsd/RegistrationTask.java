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
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import by.klnvch.link5dots.multiplayer.targets.TargetNsd;
import by.klnvch.link5dots.multiplayer.utils.OnTargetCreatedListener;
import by.klnvch.link5dots.multiplayer.utils.OnTargetDeletedListener;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class RegistrationTask implements NsdManager.RegistrationListener {

    private static final String TAG = "RegistrationTask";

    private OnTargetCreatedListener mCreateListener;
    private OnTargetDeletedListener mDeleteListener;

    public void registerService(int port,
                                @NonNull OnTargetCreatedListener createdListener) {
        checkState(port > -1);
        checkNotNull(createdListener);

        this.mCreateListener = createdListener;

        NsdHelper.registerService(port, this);
    }

    public void unregisterService(@Nullable OnTargetDeletedListener deletedListener) {
        this.mDeleteListener = deletedListener;
        NsdHelper.unregisterService(this);
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(TAG, "onRegistrationFailed: " + errorCode);

        mCreateListener.onTargetCreationFailed(new Exception("onRegistrationFailed: " + errorCode));
        mCreateListener = null;
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        Log.e(TAG, "onUnregistrationFailed: " + errorCode);

        if (mDeleteListener != null) {
            mDeleteListener.onTargetDeleted(new Exception("onUnregistrationFailed: " + errorCode));
            mDeleteListener = null;
        }
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "onServiceRegistered: " + serviceInfo);

        mCreateListener.onTargetCreated(new TargetNsd(serviceInfo));
        mCreateListener = null;
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
        Log.d(TAG, "onServiceUnregistered: " + serviceInfo);

        if (mDeleteListener != null) {
            mDeleteListener.onTargetDeleted(null);
            mDeleteListener = null;
        }
    }
}