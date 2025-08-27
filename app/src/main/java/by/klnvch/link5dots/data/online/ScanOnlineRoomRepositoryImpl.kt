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

import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.online.models.AcceptOnlineRoomInvitation
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.models.online.OnlineRoomInvitationRemote
import by.klnvch.link5dots.domain.models.online.toOnlineRoomInvitation
import by.klnvch.link5dots.domain.repositories.ConnectOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.ScanOnlineRoomRepository
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScanOnlineRoomRepositoryImpl @Inject constructor(
    private val connectRepository: ConnectOnlineRoomRepository,
) : ScanOnlineRoomRepository {
    private val path = if (BuildConfig.DEBUG) "rooms_debug" else "rooms_v2"
    private val reference = Firebase.database.reference.child(path)

    override fun getInvitations() = reference
        .orderByChild("state")
        .equalTo(RoomState.CREATED.ordinal.toDouble())
        .snapshots
        .map { it.children }
        .map {
            it.mapNotNull { snapshot ->
                snapshot.toInvitation(connectRepository::connect)
            }
        }

    private fun DataSnapshot.toInvitation(
        onConnect: suspend (key: String, accept: AcceptOnlineRoomInvitation) -> Unit,
    ) = toOnlineRoomInvitation(key, getValue(OnlineRoomInvitationRemote::class.java), onConnect)
}
