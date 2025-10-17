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
import by.klnvch.link5dots.data.nsd.NsdExt.address
import by.klnvch.link5dots.data.sockets.SocketData
import by.klnvch.link5dots.data.sockets.SocketRoomRepository
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomInvitation
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject


class NsdRoomRepositoryImpl @Inject constructor(
    private val nsdValidator: NsdValidator,
    private val nsdRegistration: NsdRegistration,
    private val nsdDiscovery: NsdDiscovery,
) : SocketRoomRepository(), NsdRoomRepository {
    override val TAG = NsdParams.TAG

    override suspend fun create() {
        nsdValidator.validate()
        val serverSocket = ServerSocket(0)
        try {
            val info = nsdRegistration.register(serverSocket.localPort)
            val descriptor = NsdRoomDescriptor(info)
            startAccepting(descriptor, serverSocket) {
                val socket = serverSocket.accept()
                SocketData(socket, socket.inputStream, socket.outputStream)
            }
        } catch (e: Throwable) {
            serverSocket.closeSafely()
            delete()
            throw e
        }
    }

    override fun delete() {
        nsdRegistration.unregister()
        super.delete()
    }

    override fun getInvitations(): Flow<List<RoomInvitation>> {
        nsdValidator.validate()
        return nsdDiscovery.discover().map { list -> list.map { NsdRoomInvitationImpl(it) } }
    }

    private inner class NsdRoomInvitationImpl(
        private val info: NsdServiceInfo,
    ) : RoomInvitation {
        override val title = info.title()
        override val description = info.description()
        override val time = null
        override val isFavorite = false

        override suspend fun onConnect(user2: NetworkUser) = connect(this, user2) {
            val socket = Socket(info.address, info.port)
            SocketData(socket, socket.inputStream, socket.outputStream)
        }
    }
}

data class NsdRoomDescriptor(
    val serviceInfo: NsdServiceInfo,
) : RemoteRoomDescriptor {
    override val title = serviceInfo.title()
    override val description = serviceInfo.description()
    override val isFavorite = false
    override val time = null
}

private fun NsdServiceInfo.title() = serviceName ?: ""
private fun NsdServiceInfo.description() = listOfNotNull(
    address?.toString(),
    if (port == 0) null else port.toString()
).joinToString(":")
