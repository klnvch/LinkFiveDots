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

package by.klnvch.link5dots.multiplayer.nsd;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.ref.WeakReference;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.MultiplayerActivity;
import by.klnvch.link5dots.multiplayer.MultiplayerService;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class NsdPickerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MESSAGE_SERVER_STATE_CHANGE = 101;
    public static final int MESSAGE_CLIENT_STATE_CHANGE = 102;
    public static final int MESSAGE_SERVICES_LIST_UPDATED = 300;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private static final String TAG = "NsdPickerActivity";
    private static final int MESSAGE_STATE_CHANGE = 1;
    private static final int MESSAGE_TOAST = 5;

    private final MHandler mHandler = new MHandler(this);
    private ProgressDialog progressDialog = null;
    private ToggleButton mButtonCreate;
    private TextView mCreateStatusValue;
    private View mCreateStatusLabel;
    private View mProgressCreate;
    private ToggleButton mButtonScan;
    private View mProgressScan;
    private NsdListAdapter mAdapter;
    private NsdService mNsdService;

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MultiplayerService.LocalBinder binder = (MultiplayerService.LocalBinder) service;
            mNsdService = (NsdService) binder.getService();
            mNsdService.setHandler(mHandler);
            if (mNsdService != null && mNsdService.getState() == NsdService.STATE_CONNECTING) {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(NsdPickerActivity.this, null, getString(R.string.bluetooth_connecting), true, false, null);
                }
            }
            //
            setCreateState(mNsdService.getServerState());
            setScanState(mNsdService.getClientState());
            updateServicesList();
        }

        public void onServiceDisconnected(ComponentName name) {
            mNsdService = null;
        }
    };
    private boolean isBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nsd_picker);
        setTitle(R.string.menu_local_network);

        // NoClassDefFoundError (@by.klnvch.link5dots.activity_nsd_picker.NsdService:<init>:294) {main}
        try {
            Class.forName("android.net.nsd.NsdManager");
            setResult(RESULT_OK);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Initialize the button to perform device discovery
        mButtonCreate = findViewById(R.id.buttonCreate);
        mButtonCreate.setOnClickListener(this);

        mButtonScan = findViewById(R.id.buttonScan);
        mButtonScan.setOnClickListener(this);
        //
        mCreateStatusLabel = findViewById(R.id.textStatusLabel);
        mCreateStatusValue = findViewById(R.id.textStatusValue);
        mProgressCreate = findViewById(R.id.progressCreate);

        mProgressScan = findViewById(R.id.progressScan);

        RecyclerView recyclerView = findViewById(R.id.listDestinations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new NsdListAdapter(service -> mNsdService.connect(service));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
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
        if (mNsdService != null && mNsdService.getState() == NsdService.STATE_CONNECTING) {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(this, null, getString(R.string.bluetooth_connecting), true, false, null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
        if (progressDialog != null) {
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

    private void setCreateState(int state) {
        Log.d(TAG, "create state: " + state);
        switch (state) {
            case NsdService.STATE_UNREGISTERED:
                mButtonCreate.setChecked(false);
                mButtonCreate.setEnabled(true);

                mCreateStatusValue.setText(R.string.apn_not_set);
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case NsdService.STATE_REGISTERING:
                mButtonCreate.setChecked(false);
                mButtonCreate.setEnabled(false);

                mCreateStatusValue.setText(R.string.apn_not_set);
                mCreateStatusValue.setVisibility(View.INVISIBLE);
                mCreateStatusLabel.setVisibility(View.INVISIBLE);
                mProgressCreate.setVisibility(View.VISIBLE);
                break;
            case NsdService.STATE_REGISTERED:
                mButtonCreate.setChecked(true);
                mButtonCreate.setEnabled(true);

                if (mNsdService != null) {
                    mCreateStatusValue.setText(mNsdService.getServiceName());
                } else {
                    mCreateStatusValue.setText(null);
                }
                mCreateStatusValue.setVisibility(View.VISIBLE);
                mCreateStatusLabel.setVisibility(View.VISIBLE);
                mProgressCreate.setVisibility(View.INVISIBLE);
                break;
            case NsdService.STATE_UNREGISTERING:
                mButtonCreate.setChecked(true);
                mButtonCreate.setEnabled(false);

                mCreateStatusValue.setText(R.string.apn_not_set);
                mCreateStatusValue.setVisibility(View.INVISIBLE);
                mCreateStatusLabel.setVisibility(View.INVISIBLE);
                mProgressCreate.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setScanState(int state) {
        Log.d(TAG, "scan state: " + state);
        switch (state) {
            case NsdService.STATE_IDLE:
                mButtonScan.setChecked(false);
                mProgressScan.setVisibility(View.INVISIBLE);
                break;
            case NsdService.STATE_DISCOVERING:
                mButtonScan.setChecked(true);
                mProgressScan.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void updateServicesList() {
        mAdapter.replaceAll(mNsdService.getServices());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCreate:
                if (mButtonCreate.isChecked()) {
                    mNsdService.registerService();
                } else {
                    mNsdService.unRegisterService();
                }
                break;
            case R.id.buttonScan:
                if (mButtonScan.isChecked()) {
                    mNsdService.discoverServices();
                } else {
                    mNsdService.stopDiscovery();
                    mAdapter.clear();
                }
                break;
        }
    }

    // The Handler that gets information back from the BluetoothService
    private static class MHandler extends Handler {
        private final WeakReference<NsdPickerActivity> mActivity;

        MHandler(NsdPickerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            NsdPickerActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_SERVER_STATE_CHANGE:
                        activity.setCreateState(msg.arg1);
                        break;
                    case MESSAGE_CLIENT_STATE_CHANGE:
                        activity.setScanState(msg.arg1);
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
                                if (activity.progressDialog != null) {
                                    activity.progressDialog.cancel();
                                    activity.progressDialog = null;
                                }
                                break;
                        }
                        break;
                    case MultiplayerActivity.MESSAGE_DEVICE_NAME:
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
}