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

package by.klnvch.link5dots.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun Dots() {
    Row(modifier = Modifier.background(Color.Black)) {
        RedDot()
        BlueDot()
        RedCross()
        BlueCircle()
    }
}

@Composable
fun RedDot() {
    Icon(imageVector = Dot, contentDescription = null, tint = Color.Red)
}

@Composable
fun BlueDot() {
    Icon(imageVector = Dot, contentDescription = null, tint = Color.Blue)
}

@Composable
fun RedCross() {
    Icon(imageVector = Cross, contentDescription = null, tint = Color.Red)
}

@Composable
fun BlueCircle() {
    Icon(imageVector = Circle, contentDescription = null, tint = Color.Blue)
}

val Dot: ImageVector
    get() {
        if (dot != null) return dot!!

        dot = ImageVector.Builder(
            name = "Dot",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(2f, 12f)
                arcToRelative(
                    a = 10f,
                    b = 10f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 20f,
                    dy1 = 0f
                )
                arcToRelative(
                    a = 10f,
                    b = 10f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -20f,
                    dy1 = 0f
                )
                close()
            }
        }.build()

        return dot!!
    }


val Circle: ImageVector
    get() {
        if (circle != null) return circle!!

        circle = ImageVector.Builder(
            name = "Circle",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(0f, 12f)
                arcTo(
                    12f,
                    12f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 24f,
                    y1 = 12f
                )
                arcTo(
                    12f,
                    12f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    x1 = 0f,
                    y1 = 12f
                )
                moveTo(4f, 12f)
                arcTo(
                    8f,
                    8f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 20f,
                    y1 = 12f
                )
                arcTo(
                    8f,
                    8f,
                    theta = 0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 4f,
                    y1 = 12f
                )
                close()
            }
        }.build()

        return circle!!
    }

val Cross: ImageVector
    get() {
        if (cross != null) return cross!!

        cross = ImageVector.Builder(
            name = "Cross",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black), pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3f, 0f)
                lineTo(0f, 3f)
                lineTo(9f, 12f)
                lineTo(0f, 21f)
                lineTo(3f, 24f)
                lineTo(12f, 15f)
                lineTo(21f, 24f)
                lineTo(24f, 21f)
                lineTo(15f, 12f)
                lineTo(24f, 3f)
                lineTo(21f, 0f)
                lineTo(12f, 9f)
                close()
            }
        }.build()

        return cross!!
    }

private var dot: ImageVector? = null
private var circle: ImageVector? = null
private var cross: ImageVector? = null
