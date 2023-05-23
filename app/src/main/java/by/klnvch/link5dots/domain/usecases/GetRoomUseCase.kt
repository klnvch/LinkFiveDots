/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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
package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.NetworkRoomExtended
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.repositories.FirebaseManager
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.Settings
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface GetRoomUseCase : GetUseCase<IRoom, RoomParam>

class GetRoomOfflineUseCase @Inject constructor(
    private val repository: RoomRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByType -> repository.getRecentByType(param.type)
        is RoomByKey -> repository.getByKey(param.key)
        else -> throw IllegalArgumentException("Wrong param")
    }
}

class GetRoomOnlineUseCase @Inject constructor(
    private val repository: OnlineRoomRepository,
    private val firebaseManager: FirebaseManager,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByDescriptor -> repository.get(param.descriptor)
            .map { NetworkRoomExtended(it, firebaseManager.getUserId()) }

        else -> throw IllegalArgumentException("Wrong param")
    }
}

class GetRoomNsdUseCase @Inject constructor(
    private val repository: NsdRoomRepository,
    private val settings: Settings,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByDescriptor -> repository
            .get(param.descriptor)
            .combine(settings.getUserId())
            { room, userId -> NetworkRoomExtended(room, userId) }

        else -> throw IllegalArgumentException("Wrong param")
    }
}

sealed interface RoomParam
data class RoomByType(val type: Int) : RoomParam
data class RoomByKey(val key: String) : RoomParam
data class RoomByDescriptor(val descriptor: RemoteRoomDescriptor) : RoomParam
