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
@JsExport()
interface IRoom {
    val key: String
    val time: Int
    val dots: List<Dot>
    val user1: IUser?
    val user2: IUser?
    val type: Int
    fun getDuration() = dots.lastOrNull()?.dt ?: 0
    fun getEndTime() = time + getDuration()
    fun getWinningLine() = dots.findWinningLine()
    fun isNotOver() = getWinningLine() == null
    fun isOver() = getWinningLine() != null
    fun isFree(p: Point) = dots.find { it.x == p.x && it.y == p.y } == null
}

fun List<Dot>.findWinningLine(): WinningLine? {
    if (size < 9) return null

    val lastDot = last()
    val points = filter { it.type == lastDot.type }.map { Point(it.x, it.y) }

    // y = x + (py - px)
    // y = -x + (py + px)
    // y = py
    // x = px
    val line = points.findMaxLine { it.y == it.x + (lastDot.y - lastDot.x) }
        ?: points.findMaxLine { it.y == -it.x + (lastDot.y + lastDot.x) }
        ?: points.findMaxLine { it.y == lastDot.y }
        ?: points.map { it.invert() }.findMaxLine { it.y == lastDot.x }?.map { it.invert() }

    return if (line != null) WinningLineImpl(line, lastDot.type) else null
}

private inline fun List<Point>.findMaxLine(predicate: (Point) -> Boolean): List<Point>? = this
    .filter { p -> predicate(p) }
    .sortedBy { it.x }
    .fold<Point, List<Point>>(emptyList()) { acc, next ->
        if (acc.isEmpty()) listOf(next)
        else if (acc.last().x + 1 == next.x) acc + next
        else if (acc.size >= 5) acc
        else listOf(next)
    }.takeIf { it.size >= 5 }
