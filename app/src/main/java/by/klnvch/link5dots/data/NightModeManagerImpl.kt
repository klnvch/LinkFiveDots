/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

package by.klnvch.link5dots.data

import androidx.appcompat.app.AppCompatDelegate
import by.klnvch.link5dots.domain.models.NightMode
import by.klnvch.link5dots.domain.repositories.NightModeManager
import javax.inject.Inject

class NightModeManagerImpl @Inject constructor() : NightModeManager {
    override fun set(nightMode: String): Boolean {
        val newNightMode = map(nightMode)
        val oldNightMode = AppCompatDelegate.getDefaultNightMode()
        return if (newNightMode != oldNightMode) {
            AppCompatDelegate.setDefaultNightMode(newNightMode)
            true
        } else {
            false
        }
    }

    override fun reset() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun map(nightMode: String): Int {
        return when (nightMode) {
            NightMode.ON -> AppCompatDelegate.MODE_NIGHT_YES
            NightMode.OFF -> AppCompatDelegate.MODE_NIGHT_NO
            NightMode.AUTO -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            NightMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }
}
