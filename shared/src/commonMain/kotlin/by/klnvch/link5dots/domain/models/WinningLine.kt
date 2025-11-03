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

package by.klnvch.link5dots.domain.models

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface WinningLine {
    val points: Array<Point>
    val orientation: LineOrientation
}

class WinningLineImpl(points: List<Point>) : WinningLine {
    override val points = points.toTypedArray()
    private val first = points.first()
    private val last = points.last()
    override val orientation = when {
        first.y == last.y -> LineOrientation.HORIZONTAL
        first.x == last.x -> LineOrientation.VERTICAL
        (first.x - last.x) * (first.y - last.y) < 0 -> LineOrientation.DIAGONAL_LEFT
        else -> LineOrientation.DIAGONAL_RIGHT
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
enum class LineOrientation {
    HORIZONTAL, VERTICAL, DIAGONAL_LEFT, DIAGONAL_RIGHT
}
