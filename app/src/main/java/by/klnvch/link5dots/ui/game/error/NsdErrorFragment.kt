/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.ui.game.error

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.FeatureDisabled

class NsdErrorFragment : MultiplayerErrorFragment() {
    private val requestEnableWifiLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val error = BundleCompat.getSerializable(requireArguments(), "error", Throwable::class.java)

        if (error is FeatureDisabled) {
            binding.errorMessage.setText(R.string.wifi_settings_primary_switch_title)
            binding.errorButton.setText(R.string.confirmation_turn_on)
        }
    }

    override fun onErrorAccepted() {
        val error = BundleCompat.getSerializable(requireArguments(), "error", Throwable::class.java)

        if (error is FeatureDisabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestEnableWifiLauncher.launch(Intent(Settings.Panel.ACTION_WIFI))
            } else {
                requestEnableWifiLauncher.launch(Intent(WifiManager.ACTION_PICK_WIFI_NETWORK))
            }
        } else {
            super.onErrorAccepted()
        }
    }
}
