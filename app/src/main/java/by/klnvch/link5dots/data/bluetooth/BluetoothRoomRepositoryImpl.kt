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
import android.content.Context
import by.klnvch.link5dots.data.RoomJsonMapper
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.createServerSocket
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.deviceName
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.getDeviceAddress
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.getDeviceName
import by.klnvch.link5dots.data.bluetooth.BluetoothExt.isBonded
import by.klnvch.link5dots.data.sockets.SocketData
import by.klnvch.link5dots.data.sockets.SocketRoomRepository
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomInvitation
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

class BluetoothRoomRepositoryImpl @Inject constructor(
    context: Context,
    private val bluetoothValidator: BluetoothValidator,
    private val bluetoothDiscoveryService: BluetoothDiscoveryService,
    private val bluetoothConnectService: BluetoothConnectService,
    private val bluetoothBondedStore: BluetoothBondedStore,
    mapper: RoomJsonMapper,
) : SocketRoomRepository(mapper), BluetoothRoomRepository {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    override val TAG = BluetoothParams.TAG

    override suspend fun create() {
        bluetoothValidator.validate()
        val descriptor = BluetoothLocalRoomDescriptor()
        val serverSocket = bluetoothManager.createServerSocket()
        startAccepting(descriptor, serverSocket) {
            val socket = serverSocket.accept()
            bluetoothBondedStore.save(socket.remoteDevice)
            SocketData(socket, socket.inputStream, socket.outputStream)
        }
    }

    override fun getInvitations(): Flow<List<RoomInvitation>> {
        bluetoothValidator.validate()
        val knownFlow = flow { emitAll(bluetoothBondedStore.getKnown().asFlow()) }
        val foundFlow = bluetoothDiscoveryService.discover().filterNotNull()
        return merge(knownFlow, foundFlow)
            .scan(emptyMap<String, BluetoothDevice>()) { acc, d -> acc.plus(d.address to d) }
            .map { it.values }
            .map { devices ->
                devices.sortedWith(
                    compareBy<BluetoothDevice> { !it.isBonded }.thenBy(
                        nullsLast()
                    ) { it.deviceName })
            }
            .map { devices -> devices.map { BluetoothRoomInvitationImpl(it) } }
    }

    private inner class BluetoothLocalRoomDescriptor() : RemoteRoomDescriptor {
        override val title = bluetoothManager.getDeviceName()
        override val description = bluetoothManager.getDeviceAddress()
        override val isFavorite = false
        override val time = null
    }

    private inner class BluetoothRoomInvitationImpl(
        private val device: BluetoothDevice,
    ) : RoomInvitation {
        override val title get() = device.deviceName ?: ""
        override val description get() = device.address ?: ""
        override val isFavorite get() = device.isBonded
        override val time = null
        override suspend fun onConnect(user2: NetworkUser) = connect(this, user2) {
            val socket = bluetoothConnectService.connect(device)
            bluetoothBondedStore.save(device)
            SocketData(socket, socket.inputStream, socket.outputStream)
        }
    }
}
