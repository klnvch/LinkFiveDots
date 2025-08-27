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

package by.klnvch.link5dots.domain.models.online

import by.klnvch.link5dots.data.firebase.OnlineRemoteUser
import by.klnvch.link5dots.data.online.models.AcceptOnlineRoomInvitation
import by.klnvch.link5dots.domain.models.NetworkUser
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport()
data class OnlineRoomInvitationRemote(
    val time: Double? = null,
    val user1: OnlineRemoteUser? = null,
)

class OnlineRoomInvitation(
    val time: Int,
    val user1: NetworkUser,
    val onConnect: suspend (accept: AcceptOnlineRoomInvitation) -> Unit,
)

inline fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

private fun OnlineRemoteUser.toNetworkUser() = id?.let { NetworkUser(it, name) }
private fun OnlineRoomInvitationRemote.toOnlineRoomInvitation(
    key: String,
    onConnect: suspend (key: String, accept: AcceptOnlineRoomInvitation) -> Unit,
) =
    safeLet(time, user1?.toNetworkUser()) { t, u ->
        OnlineRoomInvitation(
            (t / 1000).toInt(),
            u,
        ) { accept -> onConnect(key, accept) }
    }

fun toOnlineRoomInvitation(
    key: String?,
    value: OnlineRoomInvitationRemote?,
    onConnect: suspend (key: String, accept: AcceptOnlineRoomInvitation) -> Unit,
): OnlineRoomInvitation? {
    return key?.let { value?.toOnlineRoomInvitation(it, onConnect) }
}

