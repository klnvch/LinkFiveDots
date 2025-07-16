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

package by.klnvch.link5dots.ui.game

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.ceil

enum class BitmapType {
    DOT, RING, CROSS, LINE_H, LINE_V, LINE_D_L, LINE_D_R
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface GameBitmap {
    val size: Int
    val buffer: IntArray
}

fun createGameBitmap(type: BitmapType, color: Int, density: Float): GameBitmap {
    val shape = when (type) {
        BitmapType.DOT -> Circle(density)
        BitmapType.RING -> Ring(density)
        BitmapType.CROSS -> Cross(density)
        BitmapType.LINE_H -> HorizontalLine(density)
        BitmapType.LINE_V -> VerticalLine(density)
        BitmapType.LINE_D_L -> LeftDiagonalLine(density)
        BitmapType.LINE_D_R -> RightDiagonalLine(density)
    }

    val size = shape.n
    val buffer = IntArray(size * size)
    for (i in 1 until size) {
        for (j in 1 until size) {
            if (shape.isInside(i, j)) {
                buffer[j * size + i] = color
            } else {
                buffer[j * size + i] = 0
            }
        }
    }
    return GameBitmapImpl(size, buffer)
}

class GameBitmapImpl(override val size: Int, override val buffer: IntArray) : GameBitmap

private abstract class Shape(val density: Float, val size: Int) {
    abstract fun isInside(x: Int, y: Int): Boolean
    val n = ceil(density * size).toInt()

    companion object {
        const val SIZE_1 = 16
        const val SIZE_2 = 32
    }
}

private class Circle(density: Float) : Shape(density, SIZE_1) {
    private val r = size * density / 2.0
    override fun isInside(x: Int, y: Int) = (x - r) * (x - r) + (y - r) * (y - r) <= r * r
}

private class Ring(density: Float) : Shape(density, SIZE_1) {
    private val r1 = size * density / 2.0
    private val r2 = size * density / 4.0

    override fun isInside(x: Int, y: Int): Boolean {
        val d = (x - r1) * (x - r1) + (y - r1) * (y - r1)
        return d <= r1 * r1 && d >= r2 * r2
    }
}

private class Cross(density: Float) : Shape(density, SIZE_1) {
    private val r = size * density / 6.0

    override fun isInside(x: Int, y: Int): Boolean {
        val n = size * density
        return x + r >= y && x - r <= y || n - x + r >= y && n - x - r <= y
    }
}

private abstract class Line(density: Float) : Shape(density, SIZE_2) {
    protected val r = size * density / 6.0
}

private class HorizontalLine(density: Float) : Line(density) {
    override fun isInside(x: Int, y: Int): Boolean {
        val n = size * density
        return y >= (n - r) / 2.0 && y <= (n + r) / 2.0
    }
}

private class VerticalLine(density: Float) : Line(density) {
    override fun isInside(x: Int, y: Int): Boolean {
        val n = size * density
        return x >= (n - r) / 2.0 && x <= (n + r) / 2.0
    }
}

private class LeftDiagonalLine(density: Float) : Line(density) {
    override fun isInside(x: Int, y: Int): Boolean {
        return x + r >= y && x - r <= y
    }
}

private class RightDiagonalLine(density: Float) : Line(density) {
    override fun isInside(x: Int, y: Int): Boolean {
        val n = size * density
        return n - x + r >= y && n - x - r <= y
    }
}
