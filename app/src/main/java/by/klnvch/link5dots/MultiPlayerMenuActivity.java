package by.klnvch.link5dots;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import by.klnvch.link5dots.bluetooth.BluetoothService;
import by.klnvch.link5dots.bluetooth.DevicePickerActivity;
import by.klnvch.link5dots.nsd.NsdPickerActivity;
import by.klnvch.link5dots.nsd.NsdService;
import by.klnvch.link5dots.online.OnlineActivity;

public class MultiPlayerMenuActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String IS_BLUETOOTH_ENABLED = "IS_BLUETOOTH_ENABLED";

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int CHOOSE_BT_DEVICE = 4;
    private static final int CHOOSE_NSD_SERVICE = 5;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_menu);

        findViewById(R.id.multi_player_two_players).setOnClickListener(this);
        findViewById(R.id.multi_player_bluetooth).setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            findViewById(R.id.multi_player_lan).setOnClickListener(this);
        } else {
            findViewById(R.id.multi_player_lan).setVisibility(View.GONE);
        }
        findViewById(R.id.multi_player_online).setOnClickListener(this);

        // ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("EA3A211E9E56D12855FE8A22E4EB356C")
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
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
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                        // remember
                        editor.putBoolean(IS_BLUETOOTH_ENABLED, false);
                    } else {
                        // launch bluetooth device chooser
                        Intent i2 = new Intent(this, DevicePickerActivity.class);
                        startActivityForResult(i2, CHOOSE_BT_DEVICE);
                        // remember
                        editor.putBoolean(IS_BLUETOOTH_ENABLED, true);
                        // start Bluetooth service
                        startService(new Intent(this, BluetoothService.class));
                    }
                    editor.apply();
                }
                break;
            case R.id.multi_player_lan:
                Intent intent = new Intent(this, NsdPickerActivity.class);
                startActivityForResult(intent, CHOOSE_NSD_SERVICE);

                startService(new Intent(this, NsdService.class));
                break;
            case R.id.multi_player_online:
                Intent intent1 = new Intent(this, OnlineActivity.class);
                startActivity(intent1);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) {
                    Intent i2 = new Intent(this, DevicePickerActivity.class);
                    startActivityForResult(i2, CHOOSE_BT_DEVICE);
                    // start Bluetooth service
                    startService(new Intent(this, BluetoothService.class));
                }
                break;
            case CHOOSE_BT_DEVICE:
                // bluetooth game finished, make an order
                SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                if(!prefs.getBoolean(IS_BLUETOOTH_ENABLED, false)){
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBluetoothAdapter.disable();
                }
                // stop Bluetooth service
                stopService(new Intent(this, BluetoothService.class));
                break;
            case CHOOSE_NSD_SERVICE:
                stopService(new Intent(this, NsdService.class));
                break;
        }
    }
}
