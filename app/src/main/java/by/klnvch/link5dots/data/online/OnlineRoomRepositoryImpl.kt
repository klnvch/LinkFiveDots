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
import by.klnvch.link5dots.data.online.CleanUpOnlineRoomWorker.Companion.launchCleanUpOnlineRoomWorker
import by.klnvch.link5dots.data.online.mapper.toNetworkRoomState
import by.klnvch.link5dots.data.online.mapper.toOnlineRoom
import by.klnvch.link5dots.data.online.models.OnlineRoom
import by.klnvch.link5dots.data.online.models.OnlineRoomDead
import by.klnvch.link5dots.data.online.models.OnlineRoomRemote
import by.klnvch.link5dots.data.online.models.RemoteRoomItem
import by.klnvch.link5dots.data.online.models.getRoomIfAny
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.StringRepository
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import com.google.firebase.database.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class OnlineRoomRepositoryImpl @Inject constructor(
    private val context: Context,
    private val onlineLocalStore: OnlineLocalStore,
    private val stringRepository: StringRepository,
    scope: CoroutineScope,
) : OnlineRoomRepository {
    private val path = if (BuildConfig.DEBUG) "rooms_debug" else "rooms_v2"
    private val reference = Firebase.database.reference.child(path)

    private val key = onlineLocalStore.key.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val remote: Flow<OnlineRoom> = key.flatMapLatest { key ->
        reference.child(key).snapshots
            .map { it.toRemoteRoomItem() }
            .map { it.toOnlineRoom() }
    }

    private val _remoteFlow = remote
        .mapNotNull { it.getRoomIfAny() }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val roomFlow = _remoteFlow.filterNotNull()
    override val room get() = _remoteFlow.value

    override val state = remote
        .onEach { if (it is OnlineRoomDead) onlineLocalStore.clear() }
        .map { it.toNetworkRoomState(stringRepository.unknownName) }
        .distinctUntilChanged()

    override fun delete() = context.launchCleanUpOnlineRoomWorker(RoomState.DELETED)
    override fun finish() = context.launchCleanUpOnlineRoomWorker(RoomState.FINISHED)

    private fun DataSnapshot.toRemoteRoomItem() =
        RemoteRoomItem(key, getValue(OnlineRoomRemote::class.java))
}
