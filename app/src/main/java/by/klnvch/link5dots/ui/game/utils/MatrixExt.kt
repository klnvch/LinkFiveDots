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

package by.klnvch.link5dots.ui.game.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix

fun Matrix.invertMap(offset: Offset) = Matrix().apply {
    setFrom(this@invertMap)
    invert()
}.map(offset)

fun Matrix.scaleAndTranslate(zoomChange: Float, offsetChange: Offset): Matrix {
    val result = Matrix().apply { setFrom(this@scaleAndTranslate) }.also {
        it *= Matrix().apply {
            scale(zoomChange, zoomChange)
            translate(offsetChange.x, offsetChange.y)
        }
    }
    return result
}

fun Matrix.postTranslate(dx: Float, dy: Float) {
    this *= Matrix().apply { translate(dx, dy) }
}

val Matrix.scale: Float get() = this[0, 0]
