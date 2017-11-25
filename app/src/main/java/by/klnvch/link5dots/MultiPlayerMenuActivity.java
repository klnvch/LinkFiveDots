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

package by.klnvch.link5dots;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import by.klnvch.link5dots.multiplayer.bluetooth.BluetoothService;
import by.klnvch.link5dots.multiplayer.bluetooth.DevicePickerActivity;
import by.klnvch.link5dots.multiplayer.nsd.NsdPickerActivity;
import by.klnvch.link5dots.multiplayer.nsd.NsdService;
import by.klnvch.link5dots.multiplayer.online.OnlineGameActivity;

public class MultiPlayerMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String IS_BLUETOOTH_ENABLED = "IS_BLUETOOTH_ENABLED";

    private static final int RC_ENABLE_BLUETOOTH = 3;
    private static final int RC_BLUETOOTH_GAME = 4;
    private static final int RC_NSD_GAME = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_menu);
        setTitle(R.string.menu_multi_player);

        findViewById(R.id.multi_player_online).setVisibility(View.INVISIBLE);

        if (BluetoothAdapter.getDefaultAdapter() == null) {
            findViewById(R.id.multi_player_bluetooth).setVisibility(View.INVISIBLE);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            findViewById(R.id.multi_player_lan).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.multi_player_two_players:
                startActivity(new Intent(this, TwoPlayersActivity.class));
                break;
            case R.id.multi_player_bluetooth:
                // Get local Bluetooth adapter
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                // If the adapter is null, then Bluetooth is not supported
                if (mBluetoothAdapter != null) {
                    boolean isBluetoothEnabled = mBluetoothAdapter.isEnabled();
                    if (isBluetoothEnabled) {
                        startBluetoothActivity();
                    } else {
                        // enable bluetooth
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, RC_ENABLE_BLUETOOTH);
                    }
                    getPreferences(MODE_PRIVATE)
                            .edit()
                            .putBoolean(IS_BLUETOOTH_ENABLED, isBluetoothEnabled)
                            .apply();
                }
                break;
            case R.id.multi_player_lan:
                startActivityForResult(new Intent(this, NsdPickerActivity.class), RC_NSD_GAME);
                startService(new Intent(this, NsdService.class));
                break;
            case R.id.multi_player_online:
                startActivity(new Intent(this, OnlineGameActivity.class));
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_ENABLE_BLUETOOTH:
                // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) {
                    startBluetoothActivity();
                }
                break;
            case RC_BLUETOOTH_GAME:
                // bluetooth game finished, make an order
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                if (!prefs.getBoolean(IS_BLUETOOTH_ENABLED, false)) {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothAdapter.disable();
                }
                // stop Bluetooth service
                stopService(new Intent(this, BluetoothService.class));
                break;
            case RC_NSD_GAME:
                stopService(new Intent(this, NsdService.class));
                break;
        }
    }

    private void startBluetoothActivity() {
        // launch bluetooth device chooser
        Intent i = new Intent(this, DevicePickerActivity.class);
        startActivityForResult(i, RC_BLUETOOTH_GAME);
        // start Bluetooth service
        startService(new Intent(this, BluetoothService.class));
    }
}