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

package by.klnvch.link5dots.data.firebase

import by.klnvch.link5dots.domain.models.*
import javax.inject.Inject


class OnlineRoomMapper @Inject constructor() {

    fun map(room: NetworkRoom) = OnlineRoomRemote(
        room.state,
        room.dots.map { dot -> map(dot) }.ifEmpty { null },
        room.timestamp,
        map(room.user1) ?: throw IllegalStateException("user1"),
        map(room.user2),
    )

    fun map(key: String, room: OnlineRoomRemote): NetworkRoom {
        val timestamp = room.time ?: throw IllegalArgumentException("timestamp is null")
        val user1 = map(room.user1) ?: throw IllegalArgumentException("user1 is null")
        val user2 = map(room.user2)
        return NetworkRoom(
            key,
            timestamp,
            room.dots?.mapIndexed { i, d ->
                Dot(d.x!!, d.y!!, if (i % 2 == 0) Dot.HOST else Dot.GUEST, d.dt!!)
            } ?: emptyList(),
            user1,
            user2,
            RoomType.ONLINE,
            room.state ?: throw IllegalArgumentException("state is null"),
        )
    }

    private fun map(user: OnlineRemoteUser?): NetworkUser? {
        if (user != null) {
            val id = user.id ?: throw IllegalArgumentException("user.id is null")
            val name = user.name ?: throw IllegalArgumentException("user.name is null")
            return NetworkUser(id, name)
        }
        return null
    }

    fun map(user: NetworkUser?): OnlineRemoteUser? {
        return if (user != null) OnlineRemoteUser(user.id, user.name)
        else null
    }

    fun map(dot: Dot) = OnlineDotRemote(dot.dt, dot.x, dot.y)
}
