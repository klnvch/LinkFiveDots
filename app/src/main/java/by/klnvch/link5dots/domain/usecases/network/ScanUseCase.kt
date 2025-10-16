/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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

import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.RoomInvitation
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.ScanOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.ScanRoomInvitationRepository
import by.klnvch.link5dots.domain.repositories.networkUserOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ScanUseCase {
    fun scan(): Flow<List<FoundRemoteRoom>>
}

class OnlineScanUseCase @Inject constructor(
    private val repository: ScanOnlineRoomRepository,
    private val factory: ScanOnlineRoomDescriptorFactory,
) : ScanUseCase {
    override fun scan() = repository.getInvitations().map { factory.map(it) }
}

private class SocketFoundRemoteRoom(
    invitation: RoomInvitation,
    getUser: () -> NetworkUser,
) : CommonOnlineFoundRemoteRoom({ invitation.onConnect(getUser()) }) {
    override val title = invitation.title
    override val description = invitation.description
    override val time = invitation.time
    override val isFavorite = invitation.isFavorite
}

abstract class CommonScanUseCase(
    private val repository: ScanRoomInvitationRepository,
    private val networkUserProvider: NetworkUserProvider,
) : ScanUseCase {
    override fun scan(): Flow<List<FoundRemoteRoom>> = repository.getInvitations().map { list ->
        list.map { SocketFoundRemoteRoom(it) { networkUserProvider.networkUserOrThrow } }
    }
}

class NsdScanUseCase @Inject constructor(
    repository: NsdRoomRepository,
    networkUserProvider: NetworkUserProvider,
) : CommonScanUseCase(repository, networkUserProvider)

class BluetoothScanUseCase @Inject constructor(
    repository: BluetoothRoomRepository,
    networkUserProvider: NetworkUserProvider,
) : CommonScanUseCase(repository, networkUserProvider)
