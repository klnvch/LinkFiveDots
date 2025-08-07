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
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.FeatureDisabled
import by.klnvch.link5dots.domain.models.UnknownException

@Composable
fun ErrorScreenOnline(error: Throwable, onDone: (isSuccess: Boolean) -> Unit) {
    val infoTextId = when (error) {
        is FeatureDisabled -> R.string.connection_error_message
        is UnknownException -> R.string.audio_sharing_retry_dialog_content
        else -> R.string.confirmation_turn_on
    }
    val buttonTextId = when (error) {
        is FeatureDisabled -> R.string.bluetooth_devices_card_off_summary
        is UnknownException -> R.string.okay
        else -> R.string.okay
    }

    val requestEnableNetworkLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onDone(true)
        }

    ErrorScreen(infoTextId, buttonTextId) {
        if (error is FeatureDisabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestEnableNetworkLauncher.launch(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
            } else {
                requestEnableNetworkLauncher.launch(Intent(Settings.ACTION_WIRELESS_SETTINGS))
            }
        } else {
            onDone(false)
        }
    }
}
