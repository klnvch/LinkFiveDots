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
package by.klnvch.link5dots.data.online

import android.content.Context
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.firebase.OnlineRoomRemote
import by.klnvch.link5dots.data.firebase.RemoteRoomItem
import by.klnvch.link5dots.data.firebase.mapToNetworkRoom
import by.klnvch.link5dots.data.online.CleanUpOnlineRoomWorker.Companion.launchCleanUpOnlineRoomWorker
import by.klnvch.link5dots.domain.models.NetworkRoomStateDeleted
import by.klnvch.link5dots.domain.models.NetworkRoomStateFinished
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.StringRepository
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class OnlineRoomRepositoryImpl @Inject constructor(
    private val context: Context,
    private val onlineLocalStore: OnlineLocalStore,
    private val stringRepository: StringRepository,
) : OnlineRoomRepository {
    private val path = if (BuildConfig.DEBUG) "rooms_debug" else "rooms_v2"
    private val reference = Firebase.database.reference.child(path)

    private val key = onlineLocalStore.getKey().filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val remote = key.flatMapLatest { key ->
        reference.child(key).snapshots
            .map { it }
            .map { it.toRemoteRoomItem() }
    }

    override fun get() = remote.mapNotNull { it.mapToNetworkRoom() }

    override val state = get().map {
        val state = it.toNetworkRoomState(stringRepository.unknownName)
        if (state is NetworkRoomStateDeleted || state is NetworkRoomStateFinished) {
            onlineLocalStore.clear()
        }
        return@map state
    }.distinctUntilChanged()

    override fun delete() = context.launchCleanUpOnlineRoomWorker(RoomState.DELETED)

    override fun finish() = context.launchCleanUpOnlineRoomWorker(RoomState.FINISHED)

    private fun DataSnapshot.toRemoteRoomItem() =
        RemoteRoomItem(key, getValue(OnlineRoomRemote::class.java))
}
