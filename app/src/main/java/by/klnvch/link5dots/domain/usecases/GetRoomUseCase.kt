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
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomRepository
import javax.inject.Inject

abstract class GetRoomUseCase : GetUseCase<IRoom, RoomParam>

class GetOfflineRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) : GetRoomUseCase() {
    override fun get(param: RoomParam) = when (param) {
        is RoomByType -> roomRepository.getRecentByType(param.type)
        is RoomByKey -> roomRepository.getByKey(param.key)
    }
}

class GetOnlineRoomUseCase @Inject constructor(
    private val onlineRoomRepository: OnlineRoomRepository,
) : GetRoomUseCase() {
    override fun get(param: RoomParam) = when (param) {
        is RoomByType -> throw IllegalArgumentException("It is not possible to get online room by type")
        is RoomByKey -> onlineRoomRepository.get(param.key)
    }
}

sealed interface RoomParam
data class RoomByType(val type: Int) : RoomParam
data class RoomByKey(val key: String) : RoomParam
