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

package by.klnvch.link5dots.data.nsd

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import by.klnvch.link5dots.domain.models.FeatureDisabled
import by.klnvch.link5dots.domain.models.FeatureUnsupported
import javax.inject.Inject


class NsdValidator @Inject constructor(context: Context) {
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    val connectivityManager: ConnectivityManager? =
        context.getSystemService(ConnectivityManager::class.java)

    fun validate() {
        try {
            Class.forName("android.net.nsd.NsdManager")
        } catch (_: ClassNotFoundException) {
            throw FeatureUnsupported()
        }

        if (wifiManager?.isWifiEnabled != true) throw FeatureDisabled()

        val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val capabilities =
                connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
            val transportInfo = capabilities?.transportInfo
            transportInfo as? WifiInfo
        } else {
            wifiManager.connectionInfo
        }
        if (wifiInfo?.supplicantState != SupplicantState.COMPLETED) throw FeatureDisabled()
    }
}