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
package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.domain.models.RoomFactory
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowLocalRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.SocketGetFlowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface GetRoomUseCase {
    fun get(param: RoomParam): Flow<IRoom>
}

class GetRoomCommonUseCase @Inject constructor(
    private val getRepository: RoomFlowLocalRepository,
    private val saveRepository: RoomSaveLocalRepository,
    private val roomFactory: RoomFactory,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = getRepository.roomFlow
        .onEach { if (it == null) saveRepository.save(roomFactory.generate()) }
        .filterNotNull()
}

class GetRoomInfoUseCase @Inject constructor(
    private val repository: RoomRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByKey -> repository.getByKey(param.key).filterNotNull()
        else -> throw IllegalArgumentException("Wrong param")
    }
}

class GetRoomOnlineUseCase @Inject constructor(
    private val repository: OnlineRoomRepository,
    private val getRepository: GetOnlineRoomRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = repository.get().onEach { getRepository.room = it }
}

class GetRoomSocketUseCase @Inject constructor(
    private val repository: SocketGetFlowRepository,
    private val saveRepository: RoomSaveLocalRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = repository.roomFlow.onEach { saveRepository.save(it) }
}

sealed interface RoomParam
data class RoomByType(val type: RoomType) : RoomParam
data class RoomByKey(val key: String) : RoomParam
data class RoomByDescriptor(val descriptor: RemoteRoomDescriptor) : RoomParam
