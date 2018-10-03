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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

import androidx.annotation.NonNull;
import by.klnvch.link5dots.multiplayer.targets.TargetBluetooth;
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper;

public class PickerAdapterBluetooth extends PickerAdapterSockets {

    private static final String TAG = "BtPickerAdapter";

    private final Context mContext;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "receiver: " + intent);

            final BluetoothDevice device = BluetoothHelper.isDeviceFound(intent);
            if (device != null) {
                add(new TargetBluetooth(device));
                return;
            }
            if (BluetoothHelper.isDiscoveryFinished(intent)) {
                if (mOnScanStoppedListener != null) {
                    mOnScanStoppedListener.onScanStopped(null);
                    stopScan();
                }
            }
        }
    };

    public PickerAdapterBluetooth(@NonNull Context context) {
        mContext = context;
    }

    @Override
    protected void startListening() {
        clear();

        final Set<BluetoothDevice> pairedDevices = BluetoothHelper.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            add(new TargetBluetooth(device));
        }

        BluetoothHelper.registerReceiverAndStartDiscovery(mContext, mReceiver);
    }

    @Override
    protected void stopListening() {
        BluetoothHelper.unregisterReceiverAndCancelDiscovery(mContext, mReceiver);
    }
}