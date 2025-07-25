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

import by.klnvch.link5dots.data.firebase.mapToOnlineRemoteUser
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.ConnectOnlineRoomRepository

class ConnectOnlineRoomRepositoryImpl(
    private val firebaseDb: FirebaseDbSetConnected,
    private val onlineLocalStore: OnlineLocalStoreWriter,
) : ConnectOnlineRoomRepository {
    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser) {
        val key = (descriptor as OnlineRoomDescriptor).key
        firebaseDb.setConnected(
            arrayOf(key),
            RoomState.STARTED.ordinal,
            user2.mapToOnlineRemoteUser()
        )
        onlineLocalStore.save(key)
    }
}
