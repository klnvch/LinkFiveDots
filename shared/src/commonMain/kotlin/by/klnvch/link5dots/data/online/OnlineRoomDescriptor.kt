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

package by.klnvch.link5dots.data.online

import by.klnvch.link5dots.data.firebase.RemoteRoomItem
import by.klnvch.link5dots.data.firebase.mapToNetworkRoomInvitation
import by.klnvch.link5dots.domain.models.INetworkRoomInvitation
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomState
import by.klnvch.link5dots.domain.models.NetworkRoomStateCreated
import by.klnvch.link5dots.domain.models.NetworkRoomStateDeleted
import by.klnvch.link5dots.domain.models.NetworkRoomStateFinished
import by.klnvch.link5dots.domain.models.NetworkRoomStateStarted
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport()
class OnlineRoomDescriptor(
    override val title: String,
    override val time: Int,
    val key: String,
) : RemoteRoomDescriptor {
    override val description = null
    override val isFavorite = false
}

fun INetworkRoomInvitation.createDescriptor(defaultName: String) = OnlineRoomDescriptor(
    user1.name ?: defaultName,
    time,
    key,
)

fun mapToDescriptors(items: List<RemoteRoomItem>, defaultName: String) = items
    .mapNotNull { it.mapToNetworkRoomInvitation() }
    .map { it.createDescriptor(defaultName) }

@OptIn(ExperimentalJsExport::class)
@JsExport()
fun NetworkRoom.toNetworkRoomState(defaultName: String): NetworkRoomState {
    return when (state) {
        RoomState.CREATED -> NetworkRoomStateCreated(this.createDescriptor(defaultName))
        RoomState.DELETED -> NetworkRoomStateDeleted(this.createDescriptor(defaultName))
        RoomState.STARTED -> NetworkRoomStateStarted(this.createDescriptor(defaultName))
        RoomState.FINISHED -> NetworkRoomStateFinished
    }
}
