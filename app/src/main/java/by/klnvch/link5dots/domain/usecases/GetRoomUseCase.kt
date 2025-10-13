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
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.BluetoothRoomRepository
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.NsdRoomRepository
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomBotGetRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import by.klnvch.link5dots.domain.repositories.RoomTwoGetRepository
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface GetRoomUseCase : GetUseCase<IRoom, RoomParam>

class GetRoomOfflineUseCase @Inject constructor(
    private val repository: RoomRepository,
    private val botGetRepository: RoomBotGetRepository,
    private val twoGetRepository: RoomTwoGetRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByType -> when (param.type) {
            RoomType.BOT -> botGetRepository.roomFlow
            RoomType.TWO_PLAYERS -> twoGetRepository.roomFlow
            else -> repository.getRecentByType(param.type)
        }

        is RoomByKey -> repository.getByKey(param.key)
        else -> throw IllegalArgumentException("Wrong param")
    }
}

class GetRoomOnlineUseCase @Inject constructor(
    private val repository: OnlineRoomRepository,
    private val getRepository: GetOnlineRoomRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByDescriptor -> repository.get()
            .onEach { getRepository.room = it }

        else -> throw IllegalArgumentException("Wrong param")
    }
}

class GetRoomNsdUseCase @Inject constructor(
    private val dbRepository: RoomRepository,
    private val repository: NsdRoomRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByDescriptor -> repository.getFlow()
            .onEach { if (it !== null) dbRepository.save(it) }

        else -> throw IllegalArgumentException("Wrong param")
    }
}

class GetRoomBluetoothUseCase @Inject constructor(
    private val dbRepository: RoomRepository,
    private val repository: BluetoothRoomRepository,
) : GetRoomUseCase {
    override fun get(param: RoomParam) = when (param) {
        is RoomByDescriptor -> repository.getFlow()
            .onEach { if (it !== null) dbRepository.save(it) }

        else -> throw IllegalArgumentException("Wrong param")
    }
}

sealed interface RoomParam
data class RoomByType(val type: RoomType) : RoomParam
data class RoomByKey(val key: String) : RoomParam
data class RoomByDescriptor(val descriptor: RemoteRoomDescriptor) : RoomParam
