package by.klnvch.link5dots.nsd;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;

import by.klnvch.link5dots.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdPickerActivity extends Activity{

    private ProgressDialog progressDialog = null;
    private ToggleButton registerButton;
    private ToggleButton scanButton;
    private TextView registrationStatus;
    private View progressBar;
    private ServiceListAdapter mServicesListAdapter;

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
            //
            setRegisterButton(mNsdService.getServerState());
            setScanButton(mNsdService.getClientState());
            updateServicesList();
        }

        public void onServiceDisconnected(ComponentName name) {
            mNsdService = null;
        }
    };

    private final MHandler mHandler = new MHandler(this);
    private static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 4;
    private static final int MESSAGE_TOAST = 5;

    public static final int MESSAGE_SERVER_STATE_CHANGE = 101;
    public static final int MESSAGE_CLIENT_STATE_CHANGE = 102;

    public static final int MESSAGE_SERVICES_LIST_UPDATED = 300;

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
                    case MESSAGE_SERVER_STATE_CHANGE:
                        activity.setRegisterButton(msg.arg1);
                        break;
                    case MESSAGE_CLIENT_STATE_CHANGE:
                        activity.setScanButton(msg.arg1);
                        break;
                    case MESSAGE_SERVICES_LIST_UPDATED:
                        activity.updateServicesList();
                        break;
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
                        activity.startActivity(new Intent(activity, NsdActivity.class));
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
        registerButton = (ToggleButton) findViewById(R.id.register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (registerButton.isChecked()) {
                    mNsdService.registerService();
                } else {
                    mNsdService.unRegisterService();
                }
            }
        });
        scanButton = (ToggleButton) findViewById(R.id.scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanButton.isChecked()) {
                    mNsdService.discoverServices();
                } else {
                    mNsdService.stopDiscovery();
                    mServicesListAdapter.clear();
                }
            }
        });
        //
        registrationStatus = (TextView) findViewById(R.id.registration_status);
        progressBar = findViewById(R.id.progressBar);
        //
        mServicesListAdapter = new ServiceListAdapter(this);
        ListView servicesList = (ListView) findViewById(R.id.list_services);
        servicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                NsdServiceInfo nsdServiceInfo = (NsdServiceInfo)adapterView.getItemAtPosition(i);
                mNsdService.connect(nsdServiceInfo);
            }
        });
        servicesList.setAdapter(mServicesListAdapter);
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


    private void doDiscovery() {
        findViewById(R.id.label_no_device_found).setVisibility(View.GONE);
        findViewById(R.id.list_available_devices).setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        //
        mNsdService.discoverServices();
    }

    private void setRegisterButton(int state) {
        switch (state) {
            case NsdService.STATE_UNREGISTERED:
                registerButton.setChecked(false);
                registerButton.setEnabled(true);
                registrationStatus.setText("Unregistered");
                break;
            case NsdService.STATE_REGISTERING:
                registerButton.setChecked(false);
                registerButton.setEnabled(false);
                registrationStatus.setText("Registering...");
                break;
            case NsdService.STATE_REGISTERED:
                registerButton.setChecked(true);
                registerButton.setEnabled(true);
                registrationStatus.setText("Registered as \"" + mNsdService.getServiceName() + "\"");
                break;
            case NsdService.STATE_UNREGISTERING:
                registerButton.setChecked(true);
                registerButton.setEnabled(false);
                registrationStatus.setText("Un registering...");
                break;
        }
    }

    private void setScanButton(int state) {
        switch (state) {
            case NsdService.STATE_IDLE:
                scanButton.setChecked(false);
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case NsdService.STATE_DISCOVERING:
                scanButton.setChecked(true);
                progressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateServicesList(){
        mServicesListAdapter.clear();
        mServicesListAdapter.addAll(mNsdService.getServices());
        mServicesListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }
}
