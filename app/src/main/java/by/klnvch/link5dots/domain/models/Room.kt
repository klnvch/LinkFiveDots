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

sealed interface IRoom {
    val key: String
    val timestamp: Long
    val dots: List<Dot>
    val user1: IUser?
    val user2: IUser?
    val type: Int
    fun getDuration() = dots.lastOrNull()?.dt ?: 0
    fun getEndTime() = timestamp + getDuration()

    fun getWinningLine(): WinningLine? {
        if (dots.size < 9) return null

        val lastDot = getLatsDot()!!
        val points = dots.filter { it.type == lastDot.type }.map { Point(it.x, it.y) }

        // y = x + (py - px)
        // y = -x + (py + px)
        // y = py
        // x = px
        val line = points.findMaxLine { it.y == it.x + (lastDot.y - lastDot.x) }
            ?: points.findMaxLine { it.y == -it.x + (lastDot.y + lastDot.x) }
            ?: points.findMaxLine { it.y == lastDot.y }
            ?: points.map { it.invert() }.findMaxLine { it.y == lastDot.x }?.map { it.invert() }

        return if (line != null) WinningLine(line, lastDot.type) else null
    }

    fun isNotOver() = getWinningLine() == null
    fun isOver() = getWinningLine() != null

    fun getLatsDot() = dots.lastOrNull()

    fun isFree(p: Point) = dots.find { it.x == p.x && it.y == p.y } == null
}

data class Room(
    override val key: String,
    override val timestamp: Long,
    override val dots: MutableList<Dot>,
    override val user1: IUser?,
    override val user2: IUser?,
    override val type: Int,
) : IRoom {
    constructor(room: IRoom) : this(
        room.key,
        room.timestamp,
        room.dots.toMutableList(),
        room.user1,
        room.user2,
        room.type,
    )

    fun add(dot: Dot) {
        dots.add(dot)
    }

    fun undo() {
        dots.removeLast()
    }
}

interface INetworkRoom : IRoom {
    val state: Int
}

interface INetworkRoomExtended : INetworkRoom {
    val yourId: String
}

data class NetworkRoom(
    override val key: String,
    override val timestamp: Long,
    override val dots: List<Dot>,
    override val user1: NetworkUser,
    override val user2: NetworkUser?,
    override val type: Int,
    override val state: Int,
) : INetworkRoom

data class NetworkRoomExtended(
    override val key: String,
    override val timestamp: Long,
    override val dots: List<Dot>,
    override val user1: NetworkUser,
    override val user2: NetworkUser?,
    override val type: Int,
    override val state: Int,
    override val yourId: String
) : INetworkRoomExtended {
    val opponent = if (user1.id == yourId) user2 else user1

    constructor(room: NetworkRoom, yourId: String) : this(
        room.key,
        room.timestamp,
        room.dots,
        room.user1,
        room.user2,
        room.type,
        room.state,
        yourId,
    )
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
