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

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
interface IRoom {
    val key: String
    val time: Int
    val dots: List<Dot>
    val user1: IUser?
    val user2: IUser?

    fun move(dot: Dot): IRoom
    fun undo(): IRoom

    fun getDuration() = dots.lastOrNull()?.dt ?: 0
    fun getEndTime() = time + getDuration()
    fun getWinningLine() = dots.findWinningLine()
    fun isNotOver() = getWinningLine() == null
    fun isOver() = getWinningLine() != null
    fun isFree(p: Point) = dots.find { it.x == p.x && it.y == p.y } == null
}

fun IRoom.canMove(user: IUser?) = (if (dots.size % 2 == 0) user1 else user2) == user
fun IRoom?.canMove(n: Int) = if (this == null || this.isOver()) false else dots.size % 2 == n
fun IRoom?.isWon(n: Int) = if (this == null || this.isNotOver()) false else dots.size % 2 == n
fun IRoom.canUndo(user: IUser?) = (if (dots.size % 2 == 1) user1 else user2) == user
fun IRoom.isNotEmpty() = dots.isNotEmpty()
fun IRoom.lastPoint(): Point? = dots.lastOrNull()
fun IRoom.isNew() = dots.lastOrNull()?.dt == 0
val IRoom.size get() = dots.size
fun IRoom.isOwner(user: NetworkUser): Boolean = (user1 as? NetworkUser)?.id == user.id

@Serializable
data class Room(
    override val key: String,
    override val time: Int,
    override val dots: List<Dot>,
    override val user1: IUser?,
    override val user2: IUser?,
) : IRoom {
    override fun move(dot: Dot) = copy(dots = dots + dot)
    override fun undo() = copy(dots = dots.dropLast(1))
}

fun List<Point>.findWinningLine(): WinningLine? {
    if (size < 9) return null

    val lastDot = last()
    val lastIndex = lastIndex % 2

    val points = filterIndexed { i, _ -> i % 2 == lastIndex }

    // y = x + (py - px)
    // y = -x + (py + px)
    // y = py
    // x = px
    val line = points.findMaxLine { it.y == it.x + (lastDot.y - lastDot.x) }
        ?: points.findMaxLine { it.y == -it.x + (lastDot.y + lastDot.x) }
        ?: points.findMaxLine { it.y == lastDot.y }
        ?: points.map { it.invert() }.findMaxLine { it.y == lastDot.x }?.map { it.invert() }

    return if (line != null) WinningLineImpl(line) else null
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
