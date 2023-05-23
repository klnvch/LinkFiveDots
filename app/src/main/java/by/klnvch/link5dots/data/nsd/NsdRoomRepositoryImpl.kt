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
package by.klnvch.link5dots.data.nsd

import android.net.nsd.NsdServiceInfo
import android.util.Log
import by.klnvch.link5dots.data.nsd.NsdParams.TAG
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.multiplayer.utils.RoomJsonMapper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import kotlin.concurrent.thread

class NsdRoomRepositoryImpl @Inject constructor(
    private val nsdRegistration: NsdRegistration,
    private val nsdDiscovery: NsdDiscovery,
    private val mapper: RoomJsonMapper,
) : NsdRoomRepository {
    private val roomFlow = MutableSharedFlow<NetworkRoom>(1)
    private var _serverSocket: Closeable? = null
    private var _socket: Closeable? = null
    private var outputStream: DataOutputStream? = null

    override suspend fun create(room: NetworkRoom) = withContext(Dispatchers.IO) {
        val socket = ServerSocket(0)
        try {
            val info = nsdRegistration.register(socket.localPort)
            startAccepting(socket)
            roomFlow.emit(room)
            NsdRoomDescriptor(info)
        } catch (e: Throwable) {
            socket.close()
            throw e
        }
    }

    override fun getState() = roomFlow.map { it.state }

    override fun delete() {
        _serverSocket?.closeSafely()
        _serverSocket = null
        nsdRegistration.unregister()
    }

    override fun finish() {
        _socket?.closeSafely()
    }

    override fun getRemoteRooms() =
        nsdDiscovery.discover().map { list -> list.map { NsdRoomDescriptor(it) } }

    override fun get(descriptor: RemoteRoomDescriptor) = roomFlow
    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser) =
        withContext(Dispatchers.IO) {
            val info = (descriptor as NsdRoomDescriptor).serviceInfo
            val socket = Socket(info.host, info.port)

            val inputStream = DataInputStream(socket.getInputStream())
            outputStream = DataOutputStream(socket.getOutputStream())

            val json = inputStream.readUTF()
            val room = mapper.toRoom(json)
            val updatedRoom = room.copy(user2 = user2, state = RoomState.STARTED)
            roomFlow.tryEmit(updatedRoom)
            send(updatedRoom)

            _socket = socket
            startCommunication(inputStream)
        }

    override suspend fun addDot(dot: Dot) {
        val room = roomFlow.first()
        val updatedRoom = room.copy(dots = room.dots + dot)
        roomFlow.tryEmit(updatedRoom)
        send(updatedRoom)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startAccepting(serverSocket: ServerSocket) {
        thread {
            _serverSocket = serverSocket
            try {
                val socket = serverSocket.accept()

                val inputStream = DataInputStream(socket.getInputStream())
                outputStream = DataOutputStream(socket.getOutputStream())

                GlobalScope.launch(Dispatchers.IO) {
                    send(roomFlow.first())
                }

                startCommunication(inputStream)
                _socket = socket
            } catch (_: Throwable) {
                GlobalScope.launch(Dispatchers.IO) {
                    val room = roomFlow.first()
                    roomFlow.emit(room.copy(state = RoomState.DELETED))
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startCommunication(inputStream: DataInputStream) {
        thread {
            try {
                while (true) {
                    val roomJson = inputStream.readUTF()
                    val room = mapper.toRoom(roomJson)
                    roomFlow.tryEmit(room)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "${e.message}")
                GlobalScope.launch(Dispatchers.IO) {
                    val room = roomFlow.first()
                    roomFlow.tryEmit(room.copy(state = RoomState.FINISHED))
                }
            }
        }
    }

    private suspend fun send(room: NetworkRoom) = withContext(Dispatchers.IO) {
        outputStream?.let {
            val json = mapper.toJson(room)
            it.writeUTF(json)
            it.flush()
        }
    }
}

data class NsdRoomDescriptor(
    val serviceInfo: NsdServiceInfo,
) : RemoteRoomDescriptor {
    override val title: String = serviceInfo.serviceName
    override val description = "${serviceInfo.host}:${serviceInfo.port}"
}

private fun Closeable.closeSafely() = try {
    close()
} catch (e: Throwable) {
    Log.e(TAG, "${e.message}")
}
