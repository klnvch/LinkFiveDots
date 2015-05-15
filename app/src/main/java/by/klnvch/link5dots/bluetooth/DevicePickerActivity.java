package by.klnvch.link5dots.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.lang.ref.WeakReference;
import java.util.Set;

import by.klnvch.link5dots.R;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DevicePickerActivity extends Activity {

    private static final int BT_REQUEST_DISCOVERABLE = 1;

    private static final String BT_DISCOVERABLE_TIME_FINISH = "BT_DISCOVERABLE_TIME_FINISH";

    private ProgressDialog progressDialog = null;

    private BluetoothService mBluetoothService;
    private boolean isBound;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.setHandler(mHandler);
            if(mBluetoothService != null && mBluetoothService.getState() == BluetoothService.STATE_CONNECTING){
                if(progressDialog == null){
                    progressDialog = ProgressDialog.show(DevicePickerActivity.this, null, getString(R.string.bluetooth_connecting), true, false, null);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
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
        private final WeakReference<DevicePickerActivity> mActivity;

        public MHandler(DevicePickerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DevicePickerActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTING:
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
                        activity.startActivity(new Intent(activity, BluetoothActivity.class));
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
        setContentView(R.layout.bluetooth);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize the button to perform device discovery
        final Button visibilityButton = (Button) findViewById(R.id.set_visibility);
        visibilityButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Ensure this device is discoverable by others
                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                    startActivityForResult(discoverableIntent, BT_REQUEST_DISCOVERABLE);
                }
            }
        });

        //
        doDiscovery();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, 0);
        isBound = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //
        if(mBluetoothService != null && mBluetoothService.getState() == BluetoothService.STATE_CONNECTING){
            if(progressDialog == null){
                progressDialog = ProgressDialog.show(this, null, getString(R.string.bluetooth_connecting), true, false, null);
            }
        }
        //
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            long finishTime = prefs.getLong(BT_DISCOVERABLE_TIME_FINISH, -1);
            long currentTime = System.currentTimeMillis();
            if(finishTime != -1 && finishTime > currentTime){
                int interval = (int)((finishTime - currentTime) / 1000);
                setVisibilityTimer(interval);
            }
        }
        //
        setTitle(bluetoothAdapter.getName());
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

        // Make sure we're not doing discovery anymore
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtAdapter.cancelDiscovery();

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        findViewById(R.id.label_no_device_found).setVisibility(View.GONE);
        findViewById(R.id.list_available_devices).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // reset list view
        // Initialize array adapter for newly discovered devices
        DeviceListAdapter mNewDevicesArrayAdapter = new DeviceListAdapter(this);
        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.list_available_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();

        // init bonded devices
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        DeviceListAdapter mPairedDevicesArrayAdapter = new DeviceListAdapter(this);
        ListView pairedListView = (ListView) findViewById(R.id.list_paired_devices);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        if (pairedDevices.size() > 0) {
            findViewById(R.id.layout_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device);
            }
        } else {
            findViewById(R.id.layout_paired_devices).setVisibility(View.GONE);
        }
    }

    // The on-click listener for all devices in the ListViews
    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            mBtAdapter.cancelDiscovery();

            final BluetoothDevice device = (BluetoothDevice)av.getItemAtPosition(position);

            try {
                // Attempt to connect to the device
                AlertDialog.Builder builder = new AlertDialog.Builder(DevicePickerActivity.this);
                builder.setMessage(getString(R.string.bluetooth_connection_dialog_text, device.getName()));
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothService.connect(device);
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } catch (Exception e) {
                Toast.makeText(DevicePickerActivity.this, getString(R.string.bluetooth_connecting_error_message, device.getName()), Toast.LENGTH_SHORT).show();
                mBluetoothService.stop();
            }
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    ListView newDevicesListView = (ListView) findViewById(R.id.list_available_devices);
                    DeviceListAdapter mNewDevicesArrayAdapter = (DeviceListAdapter) newDevicesListView.getAdapter();
                    mNewDevicesArrayAdapter.add(device);
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                ListView newDevicesListView = (ListView) findViewById(R.id.list_available_devices);
                if(newDevicesListView.getCount() == 0){
                    newDevicesListView.setVisibility(View.GONE);
                    findViewById(R.id.label_no_device_found).setVisibility(View.VISIBLE);
                }else{
                    findViewById(R.id.label_no_device_found).setVisibility(View.GONE);
                    newDevicesListView.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        switch (requestCode){
            case BT_REQUEST_DISCOVERABLE:
                if(resultCode > 0){
                    SharedPreferences prefs = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    long finishTime = System.currentTimeMillis() + resultCode*1000;
                    editor.putLong(BT_DISCOVERABLE_TIME_FINISH, finishTime);
                    editor.commit();
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