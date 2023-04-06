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

package by.klnvch.link5dots.domain.models

data class Dot(
    val x: Int,
    val y: Int,
    val type: Int,
    val timestamp: Long,
) {
    constructor(p: Point, type: Int, timestamp: Long) : this(p.x, p.y, type, timestamp)

    companion object {
        const val EMPTY = 1
        const val HOST = 2
        const val GUEST = 4
    }
}

data class WinningLine(private val points: List<Point>, val type: Int) {
    val size = points.size
    operator fun get(i: Int) = points[i]
    private val first = points.first()
    private val last = points.last()
    val orientation = when {
        first.y == last.y -> WinningLineOrientation.HORIZONTAL
        first.x == last.x -> WinningLineOrientation.VERTICAL
        (first.x - last.x) * (first.y - last.y) < 0 -> WinningLineOrientation.DIAGONAL_LEFT
        else -> WinningLineOrientation.DIAGONAL_RIGHT
    }
}

enum class WinningLineOrientation {
    HORIZONTAL, VERTICAL, DIAGONAL_LEFT, DIAGONAL_RIGHT
}
