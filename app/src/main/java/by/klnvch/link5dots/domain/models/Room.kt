/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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

data class Room(
    override val key: String,
    override val time: Int,
    override val dots: MutableList<Dot>,
    override val user1: IUser?,
    override val user2: IUser?,
    override val type: Int,
) : IRoom {
    constructor(room: IRoom) : this(
        room.key,
        room.time,
        room.dots.toMutableList(),
        room.user1,
        room.user2,
        room.type,
    )

    fun add(dot: Dot) {
        dots.add(dot)
    }

    fun undo() {
        dots.removeAt(dots.lastIndex)
    }
}

interface INetworkRoomExtended : INetworkRoom {
    val yourId: String
}

data class NetworkRoomExtended(
    override val key: String,
    override val time: Int,
    override val dots: List<Dot>,
    override val user1: NetworkUser,
    override val user2: NetworkUser?,
    override val type: Int,
    override val state: RoomState,
    override val yourId: String,
) : INetworkRoomExtended {
    constructor(room: NetworkRoom, yourId: String) : this(
        room.key,
        room.time,
        room.dots,
        room.user1,
        room.user2,
        room.type,
        room.state,
        yourId,
    )
}
