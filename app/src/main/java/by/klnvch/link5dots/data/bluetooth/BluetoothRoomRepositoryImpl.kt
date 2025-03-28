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

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import by.klnvch.link5dots.data.RoomJsonMapper
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.FAKE_ADDRESS
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.NAME_SECURE
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.TAG
import by.klnvch.link5dots.data.bluetooth.BluetoothParams.UUID_SECURE
import by.klnvch.link5dots.domain.models.Dot
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.DataInputStream
import java.io.DataOutputStream
import javax.inject.Inject
import kotlin.concurrent.thread

class BluetoothRoomRepositoryImpl @Inject constructor(
    private val context: Context,
    private val mapper: RoomJsonMapper,
) :
    BluetoothRoomRepository {
    private val bluetoothService = context.getSystemService(BluetoothManager::class.java)
    private val roomFlow = MutableSharedFlow<NetworkRoom>(1)
    private var _serverSocket: BluetoothServerSocket? = null
    private var _socket: BluetoothSocket? = null
    private var outputStream: DataOutputStream? = null

    @SuppressLint("HardwareIds")
    override suspend fun create(room: NetworkRoom): RemoteRoomDescriptor {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw BluetoothPermissionException()
        }

        val socket =
            bluetoothService.adapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_SECURE)
        startAccepting(socket)

        roomFlow.emit(room)

        return BluetoothRoomDescriptor(
            bluetoothService.adapter.name,
            bluetoothService.adapter.address
        )
    }

    override fun getState() = roomFlow.map { it.state }

    override fun delete() {
        _serverSocket?.closeSafely()
        _serverSocket = null
    }

    override fun finish() {
        _socket?.closeSafely()
    }

    override fun getRemoteRooms(): Flow<List<RemoteRoomDescriptor>> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                this.context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw BluetoothPermissionException()
        }

        val boundedDevices: Set<BluetoothDevice> = bluetoothService.adapter.bondedDevices
        return flow {
            emit(boundedDevices.toList().map { BluetoothRemoteRoomDescriptor(it) })
        }
    }

    override fun get(descriptor: RemoteRoomDescriptor) = roomFlow

    @SuppressLint("MissingPermission")
    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser) =
        withContext(Dispatchers.IO) {
            val device = (descriptor as BluetoothRemoteRoomDescriptor).device
            val socket = device.createRfcommSocketToServiceRecord(UUID_SECURE)

            val inputStream = DataInputStream(socket.inputStream)
            outputStream = DataOutputStream(socket.outputStream)

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
    private fun startAccepting(serverSocket: BluetoothServerSocket) {
        thread {
            _serverSocket = serverSocket
            try {
                val socket = serverSocket.accept()

                val inputStream = DataInputStream(socket.inputStream)
                outputStream = DataOutputStream(socket.outputStream)

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

class BluetoothRemoteRoomDescriptor(val device: BluetoothDevice) : RemoteRoomDescriptor {
    @SuppressLint("MissingPermission")
    override val title: String = device.name
    override val description: String = device.address
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

class BluetoothPermissionException : Exception("TODO: bluetooth permission required")
