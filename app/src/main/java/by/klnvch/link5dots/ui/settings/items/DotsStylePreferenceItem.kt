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

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grain
import androidx.compose.runtime.Composable
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.ui.common.BlueCircle
import by.klnvch.link5dots.ui.common.BlueDot
import by.klnvch.link5dots.ui.common.RedCross
import by.klnvch.link5dots.ui.common.RedDot

@Composable
fun DotsStylePreferenceItem(
    value: DotsStyleType,
    onChange: (value: DotsStyleType) -> Unit,
) {
    PreferenceItem(
        imageVector = Icons.Filled.Grain,
        title = R.string.settings_dots,
        trailing = { DotsTrailing(value) },
        onClick = {
            when (value) {
                DotsStyleType.ORIGINAL -> onChange(DotsStyleType.CROSS_AND_RING)
                DotsStyleType.CROSS_AND_RING -> onChange(DotsStyleType.ORIGINAL)
            }
        },
    )
}

@Composable
private fun DotsTrailing(dotsStyleType: DotsStyleType) {
    Row {
        when (dotsStyleType) {
            DotsStyleType.ORIGINAL -> {
                RedDot()
                BlueDot()
            }

            DotsStyleType.CROSS_AND_RING -> {
                RedCross()
                BlueCircle()
            }
        }
    }
}
