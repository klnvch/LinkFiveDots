package by.klnvch.link5dots;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import by.klnvch.link5dots.bluetooth.DevicePickerActivity;
import by.klnvch.link5dots.bluetooth.BluetoothService;

public class MainMenuActivity extends Activity implements OnClickListener{

    private static final String IS_BLUETOOTH_ENABLED = "IS_BLUETOOTH_ENABLED";

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int CHOOSE_BT_DEVICE = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		
		findViewById(R.id.main_menu_single_player).setOnClickListener(this);
		findViewById(R.id.main_menu_bluetooth_game).setOnClickListener(this);
		findViewById(R.id.main_menu_scores).setOnClickListener(this);
		findViewById(R.id.main_menu_how_to).setOnClickListener(this);
        findViewById(R.id.main_menu_info).setOnClickListener(this);

        // analytics
        //((MyApplication) getApplication()).getTracker();

        // admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
	}

    @Override
    protected void onStart() {
        super.onStart();
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    public void onClick(View v) {
		
		switch (v.getId()) {
            case R.id.main_menu_single_player:
                Intent i1 = new Intent(this, MainActivity.class);
                startActivity(i1);
                break;
            case R.id.main_menu_bluetooth_game:

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
                    editor.commit();
                }
                break;
            case R.id.main_menu_scores:
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()){
                    Intent i3 = new Intent(this, ScoresActivity.class);
                    startActivity(i3);
                }else{
                    Toast.makeText(this, R.string.scores_no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.main_menu_how_to:
                Intent i4 = new Intent(this, HowToActivity.class);
                startActivity(i4);
                break;
            case R.id.main_menu_info:
                Intent i5 = new Intent(this, InfoActivity.class);
                startActivity(i5);
                break;
		}
		
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
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
        }
    }
}
