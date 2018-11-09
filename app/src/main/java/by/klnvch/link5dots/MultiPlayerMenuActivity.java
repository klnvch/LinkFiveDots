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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import by.klnvch.link5dots.multiplayer.activities.GameActivityBluetooth;
import by.klnvch.link5dots.multiplayer.activities.GameActivityNsd;
import by.klnvch.link5dots.multiplayer.activities.GameActivityOnline;
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper;

public class MultiPlayerMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String KEY_IS_BLUETOOTH_ENABLED = "KEY_IS_BLUETOOTH_ENABLED";

    private static final int RC_ENABLE_BLUETOOTH = 3;
    private static final int RC_GAME_BT = 4;
    private static final int RC_GAME_NSD = 5;
    private static final int RC_GAME_INTERNET = 6;

    private boolean mIsBluetoothEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_menu);
        setTitle(R.string.menu_multi_player);

        if (savedInstanceState != null) {
            mIsBluetoothEnabled = savedInstanceState.getBoolean(KEY_IS_BLUETOOTH_ENABLED);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_IS_BLUETOOTH_ENABLED, mIsBluetoothEnabled);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.multi_player_two_players:
                startActivity(new Intent(this, TwoPlayersActivity.class));
                break;
            case R.id.multi_player_bluetooth:
                if (BluetoothHelper.INSTANCE.isSupported()) {
                    mIsBluetoothEnabled = BluetoothHelper.INSTANCE.isEnabled();
                    if (mIsBluetoothEnabled) {
                        startBluetoothActivity();
                    } else {
                        BluetoothHelper.INSTANCE.requestEnable(this, RC_ENABLE_BLUETOOTH);
                    }
                } else {
                    showErrorDialog();
                }
                break;
            case R.id.multi_player_lan:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivityForResult(new Intent(this, GameActivityNsd.class),
                            RC_GAME_NSD);
                } else {
                    showErrorDialog();
                }
                break;
            case R.id.multi_player_online:
                startActivityForResult(new Intent(this, GameActivityOnline.class),
                        RC_GAME_INTERNET);
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
            case RC_GAME_BT:
                // bluetooth game finished, make an order
                if (!mIsBluetoothEnabled) {
                    BluetoothHelper.INSTANCE.disable();
                }
            case RC_GAME_NSD:
            case RC_GAME_INTERNET:
                if (resultCode != RESULT_OK) {
                    showErrorDialog();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startBluetoothActivity() {
        startActivityForResult(new Intent(this, GameActivityBluetooth.class), RC_GAME_BT);
    }

    private void showErrorDialog() {
        Crashlytics.log("feature not supported");
        new AlertDialog.Builder(this)
                .setMessage(R.string.error_feature_not_available)
                .setPositiveButton(R.string.okay, null)
                .show();
    }

}