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

package by.klnvch.link5dots.multiplayer.targets;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public class TargetBluetooth extends Target<BluetoothDevice> {
    public TargetBluetooth(@NonNull BluetoothDevice target) {
        super(target);
    }

    @NonNull
    @Override
    public String getShortName() {
        return getTarget().getName();
    }

    @NonNull
    @Override
    public String getLongName() {
        final BluetoothDevice device = getTarget();
        final String name = device.getName();
        final String address = device.getAddress();
        return name + '\n' + address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TargetBluetooth) {
            final TargetBluetooth targetBluetooth = (TargetBluetooth) obj;
            final BluetoothDevice bluetoothDevice = targetBluetooth.getTarget();

            return bluetoothDevice.getName().equals(getTarget().getName())
                    && bluetoothDevice.getAddress().equals(getTarget().getAddress());
        }
        return false;
    }
}