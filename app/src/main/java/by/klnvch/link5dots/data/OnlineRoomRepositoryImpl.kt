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
package by.klnvch.link5dots.data

import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.firebase.OnlineRoomMapper
import by.klnvch.link5dots.data.firebase.OnlineRoomRemote
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.StringRepository
import by.klnvch.link5dots.utils.FormatUtils.formatDateTime
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OnlineRoomRepositoryImpl @Inject constructor(
    private val mapper: OnlineRoomMapper,
    private val stringRepository: StringRepository,
) : OnlineRoomRepository {

    override val path = if (BuildConfig.DEBUG) "rooms_debug" else "rooms_v2"

    private val reference = Firebase.database.reference.child(path)

    override suspend fun generateKey() = reference.push().key ?: throw IllegalStateException()

    override suspend fun create(room: NetworkRoom) =
        suspendCancellableCoroutine { continuation ->
            val remoteRoom = mapper.map(room)

            val reference = reference
                .child(room.key)

            reference
                .setValue(remoteRoom)
                .addOnSuccessListener { continuation.resume(createDescriptor(room)) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

    override suspend fun updateState(key: String, state: Int) =
        suspendCancellableCoroutine { continuation ->
            reference
                .child(key)
                .child(CHILD_STATE)
                .setValue(state)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

    override fun getState(key: String) = callbackFlow {
        val callback = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshot.getValue(Int::class.java)
                    ?: throw IllegalStateException("Online room not found")
                trySendBlocking(state)
            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()
            }
        }

        val reference = reference.child(key).child(CHILD_STATE)

        reference.addValueEventListener(callback)
        awaitClose { reference.removeEventListener(callback) }
    }


    override fun getRemoteRooms(): Flow<List<RemoteRoomDescriptor>> = callbackFlow {
        val callback = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = dataSnapshot.children
                    .mapNotNull {
                        val key = it.key
                        val value = it.getValue(OnlineRoomRemote::class.java)
                        if (key != null && value != null) mapper.map(key, value) else null
                    }
                    .map { createDescriptor(it) }
                trySendBlocking(result)

            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()
            }
        }

        val reference = reference.orderByChild(CHILD_STATE).equalTo(RoomState.CREATED.toDouble())
        reference.addValueEventListener(callback)
        awaitClose { reference.removeEventListener(callback) }
    }

    override suspend fun isConnected() = suspendCancellableCoroutine { continuation ->
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                continuation.resume(connected)
            }

            override fun onCancelled(error: DatabaseError) =
                continuation.resumeWithException(error.toException())
        })
    }

    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser) =
        suspendCancellableCoroutine { continuation ->
            val key = (descriptor as OnlineRoomDescriptor).key

            reference
                .child(key)
                .updateChildren(
                    mapOf(
                        CHILD_STATE to RoomState.STARTED,
                        CHILD_USER2 to mapper.map(user2)
                    )
                )
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

    override fun get(descriptor: RemoteRoomDescriptor) = callbackFlow {
        val key = (descriptor as OnlineRoomDescriptor).key

        val callback = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remoteRoom = snapshot.getValue(OnlineRoomRemote::class.java)
                    ?: throw IllegalStateException("Online room not found")
                val room = mapper.map(key, remoteRoom)
                trySendBlocking(room)
            }

            override fun onCancelled(error: DatabaseError) = throw error.toException()
        }

        val reference = reference.child(key)

        reference.addValueEventListener(callback)
        awaitClose { reference.removeEventListener(callback) }
    }

    override suspend fun addDot(key: String, position: Int, dot: Dot) =
        suspendCancellableCoroutine { continuation ->
            reference
                .child(key)
                .child(CHILD_DOTS)
                .child(position.toString())
                .setValue(mapper.map(dot))
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
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
    private val room: NetworkRoom,
    private val userName: String,
) : RemoteRoomDescriptor {
    override val title = userName
    override val description = room.timestamp.formatDateTime()
    val key = room.key
}
