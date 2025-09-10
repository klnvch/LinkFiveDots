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

package by.klnvch.link5dots.data.firebase

import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.DotImpl
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomInvitation
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.RoomType
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport()
data class RemoteRoomItem(val key: String?, val value: Any?)

fun RemoteRoomItem.mapToNetworkRoomInvitation(): NetworkRoomInvitation? =
    key?.let { value?.mapToOnlineRoomRemote()?.mapToNetworkRoomInvitation(it) }

@OptIn(ExperimentalJsExport::class)
@JsExport()
fun RemoteRoomItem.mapToNetworkRoom(): NetworkRoom? =
    key?.let { value?.mapToOnlineRoomRemote()?.mapToNetworkRoom(it) }

expect fun Any.mapToOnlineRoomRemote(): OnlineRoomRemote

fun NetworkUser.mapToOnlineRemoteUser() = OnlineRemoteUser(id, name)

fun OnlineRemoteUser.mapToNetworkUser() = id?.let { NetworkUser(it, name) }

fun OnlineRoomRemote.mapToNetworkRoomInvitation(key: String): NetworkRoomInvitation? {
    val user1 = user1?.mapToNetworkUser()
    return if (time != null && user1 != null)
        NetworkRoomInvitation(key, (time / 1000).toInt(), user1, RoomType.ONLINE)
    else
        null
}

fun OnlineRoomRemote.mapToNetworkRoom(key: String): NetworkRoom {
    val timestamp = time ?: throw IllegalArgumentException("time is null")
    val user1 = user1?.mapToNetworkUser() ?: throw IllegalArgumentException("user1 is null")
    val user2 = user2?.mapToNetworkUser()
    val state = RoomState.entries[state ?: throw IllegalArgumentException("state is null")]
    return NetworkRoom(
        key,
        (timestamp / 1000).toInt(),
        dots?.mapToDotList(time) ?: emptyList(),
        user1,
        user2,
        RoomType.ONLINE,
        state,
    )
}

fun List<OnlineDotRemote>.mapToDotList(time: Long) = this
    .mapIndexed { i, d ->
        DotImpl(
            d.x!!,
            d.y!!,
            if (i % 2 == 0) Dot.HOST else Dot.GUEST,
            d.t?.let { ((it - time) / 1000).toInt() } ?: 0,
        )
    }
