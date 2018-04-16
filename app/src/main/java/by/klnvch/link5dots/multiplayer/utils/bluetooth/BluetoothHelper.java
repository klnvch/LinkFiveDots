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

package by.klnvch.link5dots.multiplayer.utils.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    private static final String NAME_SECURE = "BluetoothLinkFiveDotsSecure";
    private static final UUID UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final String FAKE_ADDRESS = "02:00:00:00:00:00";
    private static final int DISCOVERABLE_DURATION_SECONDS = 30;

    public static boolean isSupported() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static void disable() {
        BluetoothAdapter.getDefaultAdapter().disable();
    }

    public static void requestEnable(@NonNull Activity activity, int requestCode) {
        activity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                requestCode);
    }

    public static void requestDiscoverable(@NonNull Fragment fragment, int requestCode) {
        final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                .putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                        DISCOVERABLE_DURATION_SECONDS);
        fragment.startActivityForResult(intent, requestCode);
    }

    @NonNull
    public static Set<BluetoothDevice> getBondedDevices() {
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    }

    public static void registerReceiverAndStartDiscovery(@NonNull Context context,
                                                         @NonNull BroadcastReceiver receiver) {
        context.registerReceiver(receiver,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        context.registerReceiver(receiver,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        startDiscovery();
    }

    public static void unregisterReceiverAndCancelDiscovery(@NonNull Context context,
                                                            @NonNull BroadcastReceiver receiver) {
        cancelDiscovery();
        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Log.e("BluetoothUtils", "unregisterReceiver: " + e.getMessage());
        }
    }

    public static void startDiscovery() {
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    public static void cancelDiscovery() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    @Nullable
    public static BluetoothDevice isDeviceFound(@NonNull Intent intent) {
        final String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                return device;
            }
        }
        return null;
    }

    public static boolean isDiscoveryFinished(@NonNull Intent intent) {
        final String action = intent.getAction();
        return BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action);
    }

    public static BluetoothServerSocket createServerSocket() throws IOException {
        return BluetoothAdapter.getDefaultAdapter()
                .listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE);
    }

    public static BluetoothSocket createSocket(@NonNull BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(UUID_SECURE);
    }

    @NonNull
    public static String getName() {
        return BluetoothAdapter.getDefaultAdapter().getName();
    }

    @SuppressLint("HardwareIds")
    @Nullable
    public static String getAddress() {
        final String address = BluetoothAdapter.getDefaultAdapter().getAddress();
        if (FAKE_ADDRESS.equals(address)) {
            return null;
        } else {
            return address;
        }
    }
}