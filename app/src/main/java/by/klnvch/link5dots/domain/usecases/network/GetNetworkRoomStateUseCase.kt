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

import by.klnvch.link5dots.domain.models.NetworkRoomState
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GetNetworkRoomStateUseCase {
    fun get(): Flow<NetworkRoomState>
}

class GetOnlineRoomStateUseCase @Inject constructor(
    private val repository: OnlineRoomRepository,
) : GetNetworkRoomStateUseCase {
    override fun get() = repository.state
}

class GetNsdRoomStateUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
) : GetNetworkRoomStateUseCase {
    override fun get() = repository.state
}

class GetBluetoothRoomStateUseCase @Inject constructor(
    private val repository: BluetoothRoomRepository,
) : GetNetworkRoomStateUseCase {
    override fun get() = repository.state
}
