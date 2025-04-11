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
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.FAKE_ADDRESS
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.NAME_SECURE
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.UUID_SECURE
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
    private val mapper: RoomJsonMapper,
) :
    BluetoothRoomRepository {
    private val bluetoothService = context.getSystemService(BluetoothManager::class.java)
    private val roomFlow = MutableSharedFlow<NetworkRoom?>(1)
    private val stateFlow = MutableSharedFlow<Int>(1)
    private var _serverSocket: BluetoothServerSocket? = null
    private var _socket: BluetoothSocket? = null
    private var outputStream: DataOutputStream? = null

    override suspend fun create(): RemoteRoomDescriptor {
        val socket =
            bluetoothService.adapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE)
        startAccepting(socket)

        return BluetoothRoomDescriptor(
            bluetoothService.adapter.name,
            bluetoothService.adapter.address
        )
    }

    override fun getState() = stateFlow

    override fun delete() {
        _serverSocket?.closeSafely()
        _serverSocket = null
    }

    override fun finish() {
        _socket?.closeSafely()
        _socket = null
        outputStream?.closeSafely()
        outputStream = null
    }

    override fun isServer() = _serverSocket !== null

    override fun getRemoteRooms(): Flow<List<RemoteRoomDescriptor>> {
        return bluetoothDiscoveryService.discover()
            .map { it.map { BluetoothRemoteRoomDescriptor(it) } }
    }

    override suspend fun update(room: NetworkRoom) {
        Log.d(TAG, "game updated: $room")
        roomFlow.emit(room)
        send(room)
    }

    override fun get() = roomFlow

    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser): Unit =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "connect: started")
                val device = (descriptor as BluetoothRemoteRoomDescriptor).device
                val socket = bluetoothConnectService.connect(device)
                Log.d(TAG, "connect: waiting for input")

                val inputStream = DataInputStream(socket.inputStream)
                outputStream = DataOutputStream(socket.outputStream)

                val json = inputStream.readUTF()
                val room = mapper.toRoom(json)
                val updatedRoom = room.copy(user2 = user2)
                roomFlow.tryEmit(updatedRoom)
                send(updatedRoom)
                Log.d(TAG, "connect: first message received")

                _socket = socket
                startCommunication(inputStream)
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
                Log.d(TAG, "accepting: waiting")
                stateFlow.tryEmit(RoomState.CREATED)
                val socket = serverSocket.accept()
                Log.d(TAG, "accepting: connected")

                val inputStream = DataInputStream(socket.inputStream)
                outputStream = DataOutputStream(socket.outputStream)

                roomFlow.tryEmit(null)

                startCommunication(inputStream)
                _socket = socket
            } catch (e: Throwable) {
                Log.d(TAG, "accepting: ${e.message}")
                GlobalScope.launch(Dispatchers.IO) {
                    stateFlow.tryEmit(RoomState.DELETED)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startCommunication(inputStream: DataInputStream) {
        thread {
            try {
                stateFlow.tryEmit(RoomState.STARTED)
                while (true) {
                    val roomJson = inputStream.readUTF()
                    val room = mapper.toRoom(roomJson)
                    roomFlow.tryEmit(room)
                    Log.d(TAG, "received: $room")
                }
            } catch (e: Throwable) {
                Log.d(TAG, "disconnected: ${e.message}")
                GlobalScope.launch(Dispatchers.IO) {
                    val room = roomFlow.first()
                    if (room != null) {
                        roomFlow.tryEmit(room.copy(state = RoomState.FINISHED))
                    }
                    stateFlow.tryEmit(RoomState.FINISHED)
                }
            } finally {
                delete()
                finish()
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

class BluetoothRemoteRoomDescriptor(val device: BluetoothDevice) : RemoteRoomDescriptor {
    override val title get() = device.name ?: ""
    override val description get() = device.address ?: ""
}

class BluetoothRoomDescriptor(val name: String, address: String) : RemoteRoomDescriptor {
    override val title = name
    override val description = if (address === FAKE_ADDRESS) "" else address
}

private fun Closeable.closeSafely() = try {
    close()
} catch (e: Throwable) {
    Log.e(TAG, "${e.message}")
}
