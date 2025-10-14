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
interface INetworkRoomInvitation {
    val key: String
    val time: Int
    val user1: NetworkUser
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface INetworkRoom : INetworkRoomInvitation, IRoom {
    override val dots: List<Dot>
    override val user2: NetworkUser?
    val state: RoomState
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
data class NetworkRoomInvitation(
    override val key: String,
    override val time: Int,
    override val user1: NetworkUser,
) : INetworkRoomInvitation

@OptIn(ExperimentalJsExport::class)
@JsExport()
data class NetworkRoom(
    override val key: String,
    override val time: Int,
    override val dots: List<Dot>,
    override val user1: NetworkUser,
    override val user2: NetworkUser?,
    override val state: RoomState,
) : INetworkRoom {
    override fun move(dot: Dot) = copy(dots = dots + dot)
    override fun undo() = copy(dots = dots.dropLast(1))
    override fun toString() =
        "NetworkRoom(key=$key, time=$time, dots=${dots.size}, user1=${user1.name}, user2=${user2?.name})"
}
