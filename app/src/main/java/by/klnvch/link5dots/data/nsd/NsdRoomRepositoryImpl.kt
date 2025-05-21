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
package by.klnvch.link5dots.data.nsd

import android.net.nsd.NsdServiceInfo
import by.klnvch.link5dots.data.RoomJsonMapper
import by.klnvch.link5dots.data.nsd.NsdExt.address
import by.klnvch.link5dots.data.sockets.SocketData
import by.klnvch.link5dots.data.sockets.SocketRoomRepository
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject

class NsdRoomRepositoryImpl @Inject constructor(
    private val nsdRegistration: NsdRegistration,
    private val nsdDiscovery: NsdDiscovery,
    mapper: RoomJsonMapper,
) : SocketRoomRepository(mapper), NsdRoomRepository {

    override suspend fun create() = withContext(Dispatchers.IO) {
        val serverSocket = ServerSocket(0)
        try {
            val info = nsdRegistration.register(serverSocket.localPort)
            startAccepting(serverSocket) {
                val socket = serverSocket.accept()
                SocketData(socket, socket.inputStream, socket.outputStream)
            }
            NsdRoomDescriptor(info)
        } catch (e: Throwable) {
            serverSocket.close()
            throw e
        }
    }

    override fun delete() {
        nsdRegistration.unregister()
        super.delete()
    }

    override fun getRemoteRooms() =
        nsdDiscovery.discover().map { list -> list.map { NsdRoomDescriptor(it) } }


    override suspend fun connect(descriptor: RemoteRoomDescriptor, user2: NetworkUser) =
        connect(user2) {
            val info = (descriptor as NsdRoomDescriptor).serviceInfo
            val socket = Socket(info.address, info.port)
            SocketData(socket, socket.inputStream, socket.outputStream)
        }
}

data class NsdRoomDescriptor(
    val serviceInfo: NsdServiceInfo,
) : RemoteRoomDescriptor {
    override val title = serviceInfo.serviceName ?: ""
    override val description = "${serviceInfo.address}:${serviceInfo.port}"
    override val isFavorite = false
}
