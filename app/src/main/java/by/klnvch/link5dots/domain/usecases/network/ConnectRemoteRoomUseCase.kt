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
package by.klnvch.link5dots.domain.usecases.network

import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ConnectNsdRoomUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
    private val settings: Settings,
) : ConnectRemoteRoomUseCase {
    override suspend fun connect(descriptor: RemoteRoomDescriptor) {
        val userId = settings.getUserId().first()
        val userName = settings.getUserName()
        val user2 = NetworkUser(userId, userName)
        repository.connect(descriptor, user2)
    }
}

class ConnectBluetoothRoomUseCase @Inject constructor(
    private val repository: BluetoothRoomRepository,
    private val settings: Settings,
) : ConnectRemoteRoomUseCase {
    override suspend fun connect(descriptor: RemoteRoomDescriptor) {
        val userId = settings.getUserId().first()
        val userName = settings.getUserName()
        val user2 = NetworkUser(userId, userName)
        repository.connect(descriptor, user2)
    }
}
