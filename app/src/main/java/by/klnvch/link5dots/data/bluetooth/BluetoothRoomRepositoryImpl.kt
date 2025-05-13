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

package by.klnvch.link5dots.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import by.klnvch.link5dots.data.RoomJsonMapper
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.createServerSocket
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.deviceName
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.getDeviceAddress
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.getDeviceName
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.isBonded
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import javax.inject.Inject
import kotlin.concurrent.thread

class BluetoothRoomRepositoryImpl @Inject constructor(
    context: Context,
    private val bluetoothDiscoveryService: BluetoothDiscoveryService,
    private val bluetoothConnectService: BluetoothConnectService,
    private val bluetoothBondedStore: BluetoothBondedStore,
    private val mapper: RoomJsonMapper,
) :
    BluetoothRoomRepository {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val roomFlow = MutableSharedFlow<NetworkRoom?>(1)
    private val stateFlow = MutableSharedFlow<Int>(1)
    private var _serverSocket: BluetoothServerSocket? = null
    private var _socket: BluetoothSocket? = null
    private var _outputStream: DataOutputStream? = null

    override suspend fun create(): RemoteRoomDescriptor {
        val socket = bluetoothManager.createServerSocket()
        startAccepting(socket)

        return BluetoothLocalRoomDescriptor()
    }

    override fun getState() = stateFlow

    override fun delete() {
        _serverSocket?.closeSafely()
        _serverSocket = null
    }

    override fun finish() {
        _socket?.closeSafely()
        _socket = null
        _outputStream?.closeSafely()
        _outputStream = null
    }

    override fun isServer() = _serverSocket !== null

    override fun getRemoteRooms(): Flow<List<RemoteRoomDescriptor>> {
        return flow {
            val known = bluetoothBondedStore.getKnown()
            emitAll(
                bluetoothDiscoveryService
                    .discover()
                    .map { it.filter { !known.contains(it) } }
                    .map { known + it }
                    .map { it.map { BluetoothRemoteRoomDescriptor(it) } })
        }
    }

    override suspend fun update(room: NetworkRoom) {
        Log.d(TAG, "game updated: $room")
        roomFlow.emit(room)
        _outputStream?.writeRoom(room)
    }

    override fun get() = roomFlow

    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser): Unit =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "connect: started")
                val device = (descriptor as BluetoothRemoteRoomDescriptor).device
                val socket = bluetoothConnectService.connect(device)
                bluetoothBondedStore.save(device)
                val inputStream = DataInputStream(socket.inputStream)
                val outputStream = DataOutputStream(socket.outputStream)

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
                Log.d(TAG, "connect: failed ${e.message}")
                throw e
            }
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startAccepting(serverSocket: BluetoothServerSocket) {
        thread {
            _serverSocket = serverSocket
            try {
                // reset current game to generate a new one
                stateFlow.tryEmit(RoomState.CREATED)
                roomFlow.tryEmit(null)

                Log.d(TAG, "accepting: waiting")
                val socket = serverSocket.accept()
                runBlocking { bluetoothBondedStore.save(socket.remoteDevice) }
                val inputStream = DataInputStream(socket.inputStream)
                val outputStream = DataOutputStream(socket.outputStream)

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

    @OptIn(DelicateCoroutinesApi::class)
    private fun startCommunication(
        socket: BluetoothSocket,
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

    private fun DataOutputStream.writeRoom(room: NetworkRoom) {
        val json = mapper.toJson(room)
        writeUTF(json)
        flush()
    }

    private fun DataInputStream.readRoom(): NetworkRoom {
        val json = readUTF()
        return mapper.toRoom(json)
    }

    private inner class BluetoothLocalRoomDescriptor() : RemoteRoomDescriptor {
        override val title = bluetoothManager.getDeviceName()
        override val description = bluetoothManager.getDeviceAddress()
        override val isFavorite = false
    }
}

class BluetoothRemoteRoomDescriptor(val device: BluetoothDevice) : RemoteRoomDescriptor {
    override val title get() = device.deviceName ?: ""
    override val description get() = device.address ?: ""
    override val isFavorite = device.isBonded
}

private fun Closeable.closeSafely() = try {
    close()
} catch (e: Throwable) {
    Log.e(TAG, "${e.message}")
}
