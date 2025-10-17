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

package by.klnvch.link5dots.data.sockets

import android.util.Log
import by.klnvch.link5dots.data.toJson
import by.klnvch.link5dots.data.toNetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomState
import by.klnvch.link5dots.domain.models.NetworkRoomStateCreated
import by.klnvch.link5dots.domain.models.NetworkRoomStateDeleted
import by.klnvch.link5dots.domain.models.NetworkRoomStateFinished
import by.klnvch.link5dots.domain.models.NetworkRoomStateStarted
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

abstract class SocketRoomRepository {
    protected abstract val TAG: String
    private val roomFlow = MutableStateFlow<NetworkRoom?>(null)
    private val stateFlow = MutableSharedFlow<NetworkRoomState>(1)
    private var _serverSocket: Closeable? = null
    private var _socket: Closeable? = null
    private var _outputStream: DataOutputStream? = null

    protected fun startAccepting(
        descriptor: RemoteRoomDescriptor,
        serverSocket: Closeable,
        accept: suspend () -> SocketData,
    ) {
        thread {
            _serverSocket = serverSocket
            try {
                // reset current game to generate a new one
                stateFlow.tryEmit(NetworkRoomStateCreated(descriptor))
                roomFlow.tryEmit(null)

                Log.d(TAG, "accepting: waiting")
                val socketData = runBlocking { accept() }
                val socket = socketData.closable
                val inputStream = DataInputStream(socketData.inputStream)
                val outputStream = DataOutputStream(socketData.outputStream)

                Log.d(TAG, "accepting: connected")
                stateFlow.tryEmit(NetworkRoomStateStarted(descriptor))

                // get a new generated game and send it
                val newRoom = runBlocking { roomFlow.filterNotNull().first() }
                Log.d(TAG, "accepting: new room created $newRoom")
                outputStream.writeRoom(newRoom)

                // receive a game with filled user
                Log.d(TAG, "accepting: wait for user2")
                val acceptedRoom = inputStream.readRoom()
                Log.d(TAG, "accepting: new room accepted $acceptedRoom")
                roomFlow.tryEmit(acceptedRoom)

                // now we can communicate
                startCommunication(socket, outputStream, inputStream)
            } catch (e: Throwable) {
                Log.d(TAG, "accepting: ${e.message}")
                stateFlow.tryEmit(NetworkRoomStateDeleted(descriptor))
            }
        }
    }

    protected suspend fun connect(
        descriptor: RemoteRoomDescriptor,
        user2: NetworkUser,
        connect: suspend () -> SocketData,
    ): Unit =
        try {
            Log.d(TAG, "connect: started")
            val socketData = connect()
            val socket = socketData.closable
            val inputStream = DataInputStream(socketData.inputStream)
            val outputStream = DataOutputStream(socketData.outputStream)

            Log.d(TAG, "connect: connected")
            stateFlow.tryEmit(NetworkRoomStateStarted(descriptor))

            // receive new room
            Log.d(TAG, "connect: waiting for new game")
            val newRoom = inputStream.readRoom()

            // add user2 to the new room
            Log.d(TAG, "connect: new game received $newRoom")
            val roomWithUser2 = newRoom.copy(user2 = user2)
            Log.d(TAG, "connect: new game accepted $roomWithUser2")
            outputStream.writeRoom(roomWithUser2)
            roomFlow.tryEmit(roomWithUser2)

            // now we can communicate
            startCommunication(socket, outputStream, inputStream)
        } catch (e: Throwable) {
            Log.d(TAG, "connect: failed $e")
            throw e
        }

    protected fun startCommunication(
        socket: Closeable,
        outputStream: DataOutputStream,
        inputStream: DataInputStream,
    ) {
        Log.d(TAG, "connect: start communication")
        _outputStream = outputStream
        _socket = socket
        thread {
            try {
                while (true) {
                    val room = inputStream.readRoom()
                    roomFlow.tryEmit(room)
                    Log.d(TAG, "received: $room")
                }
            } catch (e: Throwable) {
                Log.d(TAG, "disconnected: ${e.message}")
                stateFlow.tryEmit(NetworkRoomStateFinished)
            } finally {
                delete()
                finish()
            }
        }
    }

    suspend fun send(room: NetworkRoom) {
        Log.d(TAG, "game updated: $room")
        roomFlow.emit(room)
        _outputStream?.writeRoom(room)
    }

    fun isServer() = _serverSocket !== null

    open fun delete() {
        _serverSocket?.closeSafely()
        _serverSocket = null
    }

    fun finish() {
        _socket?.closeSafely()
        _socket = null
        _outputStream?.closeSafely()
        _outputStream = null
    }

    val state = stateFlow

    fun getFlow() = roomFlow

    val room get() = roomFlow.value

    protected fun DataOutputStream.writeRoom(room: NetworkRoom) {
        val json = room.toJson()
        Log.d(TAG, "write: $json")
        writeUTF(json)
        flush()
    }

    protected fun DataInputStream.readRoom(): NetworkRoom {
        val json = readUTF()
        Log.d(TAG, "read: $json")
        return json.toNetworkRoom()
    }

    protected fun Closeable.closeSafely() = try {
        close()
    } catch (e: Throwable) {
        Log.e(TAG, "${e.message}")
    }
}

data class SocketData(
    val closable: Closeable,
    val inputStream: InputStream,
    val outputStream: OutputStream,
)