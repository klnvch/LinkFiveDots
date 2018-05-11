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

package by.klnvch.link5dots.multiplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper;

import static com.google.common.base.Preconditions.checkNotNull;

public class PickerFragmentBluetooth extends PickerFragment {

    private static final int REQUEST_DISCOVERABLE = 1;
    private static final int REQUEST_LOCATION = 1;

    private Button mButtonDiscoverable;
    private TextView mTextDiscoverable;
    private CountDownTimer mCountDownTimer = null;
    private long mTimerFinishTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        checkNotNull(view);

        mButtonDiscoverable = view.findViewById(R.id.buttonBluetoothVisibility);
        mButtonDiscoverable.setVisibility(View.VISIBLE);
        mButtonDiscoverable.setOnClickListener(this);

        mTextDiscoverable = view.findViewById(R.id.textBluetoothVisibility);
        mTextDiscoverable.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mTimerFinishTime = savedInstanceState.getLong("mTimerFinishTime");
            if (mTimerFinishTime != 0) {
                final long duration = mTimerFinishTime - System.currentTimeMillis();
                if (duration > 0) {
                    startTimer(duration);
                } else {
                    mTimerFinishTime = 0;
                }
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("mTimerFinishTime", mTimerFinishTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        super.onDestroyView();
    }

    @Override
    protected void onScanButtonClicked() {
        if (getActivity() == null) return;

        if (mButtonScan.isChecked()) {
            if (isPermissionNeeded() && !isPermissionGranted()) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION);
            } else {
                mListener.onStartScan();
            }
        } else {
            mListener.onStopScan();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonBluetoothVisibility:
                BluetoothHelper.requestDiscoverable(this, REQUEST_DISCOVERABLE);
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mListener.onStartScan();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean isPermissionGranted() {
        checkNotNull(getActivity());

        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isPermissionNeeded() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_DISCOVERABLE:
                if (resultCode != Activity.RESULT_CANCELED) {
                    startTimer(resultCode * 1000);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startTimer(long duration) {
        mButtonDiscoverable.setVisibility(View.GONE);

        mTimerFinishTime = System.currentTimeMillis() + duration;
        mCountDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String time = DateUtils.formatElapsedTime(millisUntilFinished / 1000);
                mTextDiscoverable.setText(getString(R.string.bluetooth_is_discoverable, time));
            }

            @Override
            public void onFinish() {
                mButtonDiscoverable.setVisibility(View.VISIBLE);
                mTextDiscoverable.setText(R.string.bluetooth_only_visible_to_paired_devices);
                mCountDownTimer = null;
                mTimerFinishTime = 0;
            }
        }.start();
    }
}