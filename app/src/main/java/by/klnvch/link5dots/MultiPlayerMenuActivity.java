package by.klnvch.link5dots;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import by.klnvch.link5dots.bluetooth.BluetoothService;
import by.klnvch.link5dots.bluetooth.DevicePickerActivity;
import by.klnvch.link5dots.nsd.NsdPickerActivity;
import by.klnvch.link5dots.nsd.NsdService;
import by.klnvch.link5dots.online.OnlineGameActivity;

public class MultiPlayerMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String IS_BLUETOOTH_ENABLED = "IS_BLUETOOTH_ENABLED";

    private static final int RC_ENABLE_BLUETOOTH = 3;
    private static final int RC_BLUETOOTH_GAME = 4;
    private static final int RC_NSD_GAME = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_menu);

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
                Intent twoPlayersIntent = new Intent(this, TwoPlayersActivity.class);
                startActivity(twoPlayersIntent);
                break;
            case R.id.multi_player_bluetooth:
                // Get local Bluetooth adapter
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                // If the adapter is null, then Bluetooth is not supported
                if (mBluetoothAdapter != null) {
                    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (!mBluetoothAdapter.isEnabled()) {
                        // enable bluetooth
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, RC_ENABLE_BLUETOOTH);
                        // remember
                        editor.putBoolean(IS_BLUETOOTH_ENABLED, false);
                    } else {
                        // launch bluetooth device chooser
                        Intent pickerIntent = new Intent(this, DevicePickerActivity.class);
                        startActivityForResult(pickerIntent, RC_BLUETOOTH_GAME);
                        // remember
                        editor.putBoolean(IS_BLUETOOTH_ENABLED, true);
                        // start Bluetooth service
                        startService(new Intent(this, BluetoothService.class));
                    }
                    editor.apply();
                }
                break;
            case R.id.multi_player_lan:
                startActivityForResult(new Intent(this, NsdPickerActivity.class), RC_NSD_GAME);
                startService(new Intent(this, NsdService.class));
                break;
            case R.id.multi_player_online:
                int orientation = getResources().getConfiguration().orientation;
                Intent intent = new Intent(this, OnlineGameActivity.class);
                intent.putExtra("orientation", orientation);
                startActivity(intent);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_ENABLE_BLUETOOTH:
                // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) {
                    Intent i2 = new Intent(this, DevicePickerActivity.class);
                    startActivityForResult(i2, RC_BLUETOOTH_GAME);
                    // start Bluetooth service
                    startService(new Intent(this, BluetoothService.class));
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
}