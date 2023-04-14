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
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import by.klnvch.link5dots.R

class PickerFragmentBluetooth : PickerFragment() {

    private var mCountDownTimer: CountDownTimer? = null
    private var mTimerFinishTime: Long = 0L

    private val isPermissionGranted: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

    private val isPermissionNeeded: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                mListener.onStartScan()
            }
        }

    private val startDiscoverableForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_CANCELED) {
                startTimer(it.resultCode * 1000L)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBluetoothVisibility.visibility = View.VISIBLE
        binding.buttonBluetoothVisibility.setOnClickListener {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                .putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                    DISCOVERABLE_DURATION_SECONDS
                )
            startDiscoverableForResult.launch(intent)
        }

        binding.textBluetoothVisibility.visibility = View.VISIBLE

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

    override fun onScanButtonClicked(isOn: Boolean) {
        if (context == null) return

        if (isOn) {
            if (isPermissionNeeded && !isPermissionGranted) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            } else {
                mListener.onStartScan()
            }
        } else {
            mListener.onStopScan()
        }
    }

    private fun startTimer(duration: Long) {
        mTimerFinishTime = System.currentTimeMillis() + duration
        mCountDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (context != null) {
                    val time = DateUtils.formatElapsedTime(millisUntilFinished / 1000)
                    binding.textBluetoothVisibility.text =
                        getString(R.string.bluetooth_is_discoverable, time)
                }
            }

            override fun onFinish() {
                if (context != null) {
                    binding.textBluetoothVisibility.setText(R.string.bluetooth_only_visible_to_paired_devices)
                    mCountDownTimer = null
                    mTimerFinishTime = 0L
                }
            }
        }.start()
    }

    companion object {
        private const val KEY_TIMER_FINISH_TIME = "mTimerFinishTime"
        private const val DISCOVERABLE_DURATION_SECONDS = 30
    }
}
