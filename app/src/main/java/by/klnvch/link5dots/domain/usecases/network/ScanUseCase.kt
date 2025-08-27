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
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.ScanOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.ScanRoomInvitationRepository
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ScanUseCase {
    fun scan(): Flow<List<FoundRemoteRoom>>
}

class OnlineScanUseCase @Inject constructor(
    private val repository: ScanOnlineRoomRepository,
    private val factory: ScanOnlineRoomDescriptorFactory,
) : ScanUseCase {
    override fun scan() = repository.getInvitations().map { list -> factory.map(list) }
}

abstract class CommonScanUseCase(
    private val repository: ScanRoomInvitationRepository,
    private val settings: Settings,
) : ScanUseCase {
    override fun scan(): Flow<List<FoundRemoteRoom>> = flow {
        val userId = settings.getUserId().first()
        val userName = settings.getUserName()
        val user2 = NetworkUser(userId, userName)
        emitAll(repository.getInvitations().map { list ->
            list.map {
                object : FoundRemoteRoom {
                    override fun connect(
                        onSuccess: () -> Unit,
                        onError: (Throwable) -> Unit,
                    ) {
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                it.onConnect(user2)
                                onSuccess()
                            } catch (e: Throwable) {
                                onError(e)
                            }
                        }
                    }

                    override val title = it.title
                    override val description = it.description
                    override val time = it.time
                    override val isFavorite = it.isFavorite
                }
            }
        })
    }
}

class NsdScanUseCase @Inject constructor(
    repository: NsdRoomRepository,
    settings: Settings,
) : CommonScanUseCase(repository, settings)

class BluetoothScanUseCase @Inject constructor(
    repository: BluetoothRoomRepository,
    settings: Settings,
) : CommonScanUseCase(repository, settings)
