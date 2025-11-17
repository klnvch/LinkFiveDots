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

package by.klnvch.link5dots.ui.settings.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.NightMode

private fun getNextValue(value: NightMode) = when (value) {
    NightMode.System -> NightMode.Off
    NightMode.Off -> NightMode.On
    NightMode.On -> NightMode.System
}

@Composable
private fun getText(value: NightMode) = when (value) {
    NightMode.System -> stringResource(R.string.settings_system)
    NightMode.Off -> stringResource(R.string.switch_off_text)
    NightMode.On -> stringResource(R.string.switch_on_text)
}

@Composable
fun NightModePreferenceItem(
    value: NightMode,
    onChange: (value: NightMode) -> Unit,
) {
    PreferenceItem(
        imageVector = Icons.Filled.WbSunny,
        title = R.string.settings_night_mode,
        value = getText(value),
        onClick = { onChange(getNextValue(value)) },
    )
}
