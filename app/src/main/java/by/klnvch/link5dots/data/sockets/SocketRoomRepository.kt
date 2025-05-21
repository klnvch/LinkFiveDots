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
import by.klnvch.link5dots.data.RoomJsonMapper
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RoomState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

abstract class SocketRoomRepository(private val mapper: RoomJsonMapper) {
    private val roomFlow = MutableSharedFlow<NetworkRoom?>(1)
    private val stateFlow = MutableSharedFlow<Int>(1)
    private var _serverSocket: Closeable? = null
    private var _socket: Closeable? = null
    private var _outputStream: DataOutputStream? = null

    protected fun startAccepting(serverSocket: Closeable, accept: suspend () -> SocketData) {
        thread {
            _serverSocket = serverSocket
            try {
                // reset current game to generate a new one
                stateFlow.tryEmit(RoomState.CREATED)
                roomFlow.tryEmit(null)

                Log.d(TAG, "accepting: waiting")
                val socketData = runBlocking { accept() }
                val socket = socketData.closable
                val inputStream = DataInputStream(socketData.inputStream)
                val outputStream = DataOutputStream(socketData.outputStream)

                Log.d(TAG, "accepting: connected")
                stateFlow.tryEmit(RoomState.STARTED)

                // get a new generated game and send it
                val newRoom = runBlocking { roomFlow.filterNotNull().first() }
                Log.d(TAG, "accepting: new room created")
                outputStream.writeRoom(newRoom)

                // receive a game with filled user
                Log.d(TAG, "accepting: wait for user2")
                val newRoomWithUser2 = inputStream.readRoom()
                roomFlow.tryEmit(newRoomWithUser2)

                // now we can communicate
                startCommunication(socket, outputStream, inputStream)
            } catch (e: Throwable) {
                Log.d(TAG, "accepting: ${e.message}")
                stateFlow.tryEmit(RoomState.DELETED)
            }
        }
    }

    protected suspend fun connect(user2: NetworkUser, connect: suspend () -> SocketData): Unit =
        try {
            Log.d(TAG, "connect: started")
            val socketData = connect()
            val socket = socketData.closable
            val inputStream = DataInputStream(socketData.inputStream)
            val outputStream = DataOutputStream(socketData.outputStream)

            Log.d(TAG, "connect: connected")
            stateFlow.tryEmit(RoomState.STARTED)

            // receive new room
            Log.d(TAG, "connect: waiting for new game")
            val newRoom = inputStream.readRoom()

            // add user2 to the new room
            Log.d(TAG, "connect: new game received $newRoom")
            val roomWithUser2 = newRoom.copy(user2 = user2)
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
                val room = runBlocking { roomFlow.first() }
                if (room != null) {
                    roomFlow.tryEmit(room.copy(state = RoomState.FINISHED))
                }
                stateFlow.tryEmit(RoomState.FINISHED)
            } finally {
                delete()
                finish()
            }
        }
    }

    suspend fun update(room: NetworkRoom) {
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

    fun getState() = stateFlow

    fun get() = roomFlow

    protected fun DataOutputStream.writeRoom(room: NetworkRoom) {
        val json = mapper.toJson(room)
        writeUTF(json)
        flush()
    }

    protected fun DataInputStream.readRoom(): NetworkRoom {
        val json = readUTF()
        return mapper.toRoom(json)
    }
}

data class SocketData(
    val closable: Closeable,
    val inputStream: InputStream,
    val outputStream: OutputStream,
)

fun Closeable.closeSafely() = try {
    close()
} catch (e: Throwable) {
    Log.e(TAG, "${e.message}")
}