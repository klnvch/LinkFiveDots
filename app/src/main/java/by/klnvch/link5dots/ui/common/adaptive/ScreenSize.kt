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

package by.klnvch.link5dots.ui.common.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass

enum class ScreenSizeType { Normal, Medium, Large }

private fun WindowSizeClass.isMedium() = isAtLeastBreakpoint(
    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
    WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
)

private fun WindowSizeClass.isExpanded() = isAtLeastBreakpoint(
    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
    WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND
)

@Composable
fun screenSize(): ScreenSizeType {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        windowSizeClass.isExpanded() -> ScreenSizeType.Large
        windowSizeClass.isMedium() -> ScreenSizeType.Medium
        else -> ScreenSizeType.Normal
    }
}

@Composable
fun adaptiveWidthInMax(): Dp {
    val screenSize = screenSize()
    return when (screenSize) {
        ScreenSizeType.Large -> 640.dp
        ScreenSizeType.Medium -> 480.dp
        ScreenSizeType.Normal -> 320.dp
    }
}

@Composable
fun adaptiveFontSize(): TextUnit {
    val screenSize = screenSize()
    return when (screenSize) {
        ScreenSizeType.Large -> 28.sp
        ScreenSizeType.Medium -> 21.sp
        ScreenSizeType.Normal -> 14.sp
    }
}
