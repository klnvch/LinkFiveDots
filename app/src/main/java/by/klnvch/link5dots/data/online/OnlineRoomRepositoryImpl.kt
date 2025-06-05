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
import by.klnvch.link5dots.data.firebase.OnlineRoomMapper
import by.klnvch.link5dots.data.firebase.OnlineRoomRemote
import by.klnvch.link5dots.data.online.CleanUpOnlineRoomWorker.Companion.launchCleanUpOnlineRoomWorker
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomCreated
import by.klnvch.link5dots.domain.models.NetworkRoomDeleted
import by.klnvch.link5dots.domain.models.NetworkRoomFinished
import by.klnvch.link5dots.domain.models.NetworkRoomStarted
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.StringRepository
import by.klnvch.link5dots.utils.FormatUtils.formatDateTime
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.snapshots
import com.google.firebase.database.values
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OnlineRoomRepositoryImpl @Inject constructor(
    private val context: Context,
    private val mapper: OnlineRoomMapper,
    private val stringRepository: StringRepository,
) : OnlineRoomRepository {
    private val path = if (BuildConfig.DEBUG) "rooms_debug" else "rooms_v2"
    private val reference = Firebase.database.reference.child(path)
    private var keyFlow = MutableStateFlow<String?>(null)
    private var _room: NetworkRoom? = null

    override suspend fun create(room: NetworkRoom) {
        val remoteRoom = mapper.map(room)
        reference.child(room.key).setValue(remoteRoom).await()
        keyFlow.emit(room.key)
    }

    override val state = get().mapNotNull {
        when (it.state) {
            RoomState.CREATED -> NetworkRoomCreated(createDescriptor(it))
            RoomState.DELETED -> NetworkRoomDeleted
            RoomState.STARTED -> NetworkRoomStarted(createDescriptor(it))
            RoomState.FINISHED -> NetworkRoomFinished
            else -> null
        }
    }.distinctUntilChanged()

    override fun getKey(): String? = keyFlow.value

    override suspend fun updateState(key: String, state: Int) {
        reference.child(key).child(CHILD_STATE).setValue(state).await()
    }

    override fun getRemoteRooms(): Flow<List<RemoteRoomDescriptor>> = reference
        .orderByChild(CHILD_STATE)
        .equalTo(RoomState.CREATED.toDouble())
        .snapshots
        .map { it.children }
        .map { it.mapNotNull { dataSnapshotToRoom(it) }.map { createDescriptor(it) } }

    override suspend fun isConnected() =
        Firebase.database.getReference(".info/connected").values<Boolean>().first() == true

    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser) {
        val key = (descriptor as OnlineRoomDescriptor).key
        reference
            .child(key)
            .updateChildren(
                mapOf(
                    CHILD_STATE to RoomState.STARTED,
                    CHILD_USER2 to mapper.map(user2)
                )
            ).await()
        keyFlow.emit(key)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun get(): Flow<NetworkRoom> = keyFlow.filterNotNull().flatMapLatest { key ->
        reference.child(key).snapshots
            .map { it }
            .mapNotNull { dataSnapshotToRoom(it) }
            .onEach { this._room = it }
    }

    override fun getRoom() = this._room

    override suspend fun addDot(key: String, position: Int, dot: Dot) {
        reference
            .child(key)
            .child(CHILD_DOTS)
            .child(position.toString())
            .setValue(mapper.map(dot))
            .await()
    }

    override fun delete() {
        keyFlow.value?.let { context.launchCleanUpOnlineRoomWorker(it, RoomState.DELETED) }
    }

    override fun finish() {
        keyFlow.value?.let { context.launchCleanUpOnlineRoomWorker(it, RoomState.FINISHED) }
    }

    private fun dataSnapshotToRoom(snapshot: DataSnapshot): NetworkRoom? {
        val key = snapshot.key
        val value = snapshot.getValue(OnlineRoomRemote::class.java)
        return if (key != null && value != null) mapper.map(key, value) else null
    }

    private fun createDescriptor(room: NetworkRoom) = OnlineRoomDescriptor(
        room,
        room.user1.name.ifEmpty { stringRepository.getUnknownName() },
    )

    companion object {
        private const val CHILD_STATE = "state"
        private const val CHILD_USER2 = "user2"
        private const val CHILD_DOTS = "dots"
    }
}

data class OnlineRoomDescriptor(
    override val title: String,
    override val description: String,
    override val isFavorite: Boolean,
    val key: String,
) : RemoteRoomDescriptor {
    constructor(
        room: NetworkRoom,
        userName: String,
    ) : this(userName, room.timestamp.formatDateTime(), false, room.key)
}
