package by.klnvch.link5dots.nsd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;

import by.klnvch.link5dots.R;

public class NsdPickerActivity extends Activity{

    private static final int BT_REQUEST_DISCOVERABLE = 1;

    private static final String BT_DISCOVERABLE_TIME_FINISH = "BT_DISCOVERABLE_TIME_FINISH";

    private ProgressDialog progressDialog = null;

    private NsdService mNsdService;
    private boolean isBound;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            NsdService.LocalBinder binder = (NsdService.LocalBinder) service;
            mNsdService = binder.getService();
            mNsdService.setHandler(mHandler);
            if(mNsdService != null && mNsdService.getState() == NsdService.STATE_CONNECTING){
                if(progressDialog == null){
                    progressDialog = ProgressDialog.show(NsdPickerActivity.this, null, getString(R.string.bluetooth_connecting), true, false, null);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mNsdService = null;
        }
    };

    private final MHandler mHandler = new MHandler(this);
    private static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 4;
    private static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // The Handler that gets information back from the BluetoothService
    private static class MHandler extends Handler {
        private final WeakReference<NsdPickerActivity> mActivity;

        public MHandler(NsdPickerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            NsdPickerActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case NsdService.STATE_CONNECTING:
                                activity.progressDialog = ProgressDialog.show(activity, null, activity.getString(R.string.bluetooth_connecting), true, false, null);
                                break;
                            default:
                                if(activity.progressDialog != null){
                                    activity.progressDialog.cancel();
                                    activity.progressDialog = null;
                                }
                                break;
                        }
                        break;
                    case MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                        Toast.makeText(activity.getApplicationContext(),
                                activity.getString(R.string.bluetooth_connected, mConnectedDeviceName), Toast.LENGTH_SHORT).show();
                        activity.startActivity(new Intent(activity, NsdService.class));
                        break;
                    case MESSAGE_TOAST:
                        int msgId = msg.getData().getInt(TOAST);
                        String deviceName = msg.getData().getString(DEVICE_NAME);
                        Toast.makeText(activity.getApplicationContext(), activity.getString(msgId, deviceName), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        setContentView(R.layout.nsd);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize the button to perform device discovery
        final Button registerButton = (Button) findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mNsdService.registerService();
            }
        });

        //
        doDiscovery();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NsdService.class);
        bindService(intent, mConnection, 0);
        isBound = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //
        if(mNsdService != null && mNsdService.getState() == NsdService.STATE_CONNECTING){
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(this, null, getString(R.string.bluetooth_connecting), true, false, null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
        if(progressDialog != null){
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        findViewById(R.id.label_no_device_found).setVisibility(View.GONE);
        findViewById(R.id.list_available_devices).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        switch (requestCode){
            case BT_REQUEST_DISCOVERABLE:
                if(resultCode > 0){
                    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    long finishTime = System.currentTimeMillis() + resultCode*1000;
                    editor.putLong(BT_DISCOVERABLE_TIME_FINISH, finishTime);
                    editor.apply();
                    setVisibilityTimer(resultCode);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.bt_menu_refresh:
                doDiscovery();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    private CountDownTimer countDownTimer = null;
    private void setVisibilityTimer(int timeSeconds){
        if(countDownTimer == null) {
            final TextView visibilityInfo = (TextView) findViewById(R.id.visibility_info);
            countDownTimer = new CountDownTimer(timeSeconds * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    millisUntilFinished = millisUntilFinished / 1000;
                    int min = (int) (millisUntilFinished / 60);
                    int sec = (int) (millisUntilFinished % 60);
                    String time = String.format("%d:%02d", min, sec);
                    visibilityInfo.setText(getString(R.string.bluetooth_is_discoverable, time));
                }

                @Override
                public void onFinish() {
                    visibilityInfo.setText(R.string.bluetooth_only_visible_to_paired_devices);
                    countDownTimer = null;
                }
            }.start();
        }
    }
}
