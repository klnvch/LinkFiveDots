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

package by.klnvch.link5dots.multiplayer.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.view.View
import androidx.core.content.ContextCompat
import by.klnvch.link5dots.R
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper
import kotlinx.android.synthetic.main.fragment_game_picker.*

class PickerFragmentBluetooth : PickerFragment() {

    private var mCountDownTimer: CountDownTimer? = null
    private var mTimerFinishTime: Long = 0L

    private val isPermissionGranted: Boolean
        get() {
            return ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

    private val isPermissionNeeded: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonBluetoothVisibility.visibility = View.VISIBLE
        buttonBluetoothVisibility.setOnClickListener(this)

        textBluetoothVisibility.visibility = View.VISIBLE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            mTimerFinishTime = savedInstanceState.getLong(KEY_TIMER_FINISH_TIME)
            if (mTimerFinishTime != 0L) {
                val duration = mTimerFinishTime - System.currentTimeMillis()
                if (duration > 0) {
                    startTimer(duration)
                } else {
                    mTimerFinishTime = 0L
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(KEY_TIMER_FINISH_TIME, mTimerFinishTime)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mCountDownTimer?.cancel()
        mCountDownTimer = null
        super.onDestroyView()
    }

    override fun onScanButtonClicked() {
        if (context == null) return

        if (mButtonScan.isChecked) {
            if (isPermissionNeeded && !isPermissionGranted) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_LOCATION)
            } else {
                mListener.onStartScan()
            }
        } else {
            mListener.onStopScan()
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.buttonBluetoothVisibility) {
            BluetoothHelper.requestDiscoverable(this, REQUEST_DISCOVERABLE)
        } else {
            super.onClick(v)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mListener.onStartScan()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_DISCOVERABLE) {
            if (resultCode != Activity.RESULT_CANCELED) {
                startTimer(resultCode * 1000L)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startTimer(duration: Long) {
        mTimerFinishTime = System.currentTimeMillis() + duration
        mCountDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (context != null) {
                    val time = DateUtils.formatElapsedTime(millisUntilFinished / 1000)
                    textBluetoothVisibility.text = getString(R.string.bluetooth_is_discoverable, time)
                }
            }

            override fun onFinish() {
                if (context != null) {
                    textBluetoothVisibility.setText(R.string.bluetooth_only_visible_to_paired_devices)
                    mCountDownTimer = null
                    mTimerFinishTime = 0L
                }
            }
        }.start()
    }

    companion object {
        private const val REQUEST_DISCOVERABLE = 1
        private const val REQUEST_LOCATION = 1
        private const val KEY_TIMER_FINISH_TIME = "mTimerFinishTime"
    }
}