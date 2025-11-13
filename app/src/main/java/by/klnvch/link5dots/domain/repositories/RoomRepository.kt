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

package by.klnvch.link5dots.domain.repositories

import by.klnvch.link5dots.domain.models.HistoryRoom
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.RoomTypeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

interface RoomRepository {
    suspend fun sync(isTestDevice: Boolean)
    suspend fun delete(key: String)
    fun getAll(types: List<RoomType>): Flow<List<HistoryRoom>>
    fun getByKey(key: String): Flow<IRoom?>
    fun getRecentByType(type: RoomType): Flow<IRoom?>
    suspend fun save(room: IRoom, roomType: RoomType)
    suspend fun deleteAll()
}

interface RoomFlowLocalRepository {
    val roomFlow: Flow<IRoom?>
}

class RoomFlowLocalRepositoryImpl @Inject constructor(
    repository: RoomRepository,
    roomTypeProvider: RoomTypeProvider,
) : RoomFlowLocalRepository {
    override val roomFlow = repository.getRecentByType(roomTypeProvider.type)
}

class RoomSaveLocalRepositoryImpl @Inject constructor(
    private val repository: RoomRepository,
    private val roomTypeProvider: RoomTypeProvider,
) : RoomSaveLocalRepository {
    override suspend fun save(room: IRoom) = repository.save(room, roomTypeProvider.type)
}

abstract class RoomStateGetRepository(
    repository: RoomRepository,
    type: RoomType,
) : RoomGetRepository {
    private val roomFlow = repository.getRecentByType(type).stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.Eagerly,
        initialValue = null
    )
    override val room get() = roomFlow.value
}

@Singleton
class RoomBotGetRepository @Inject constructor(
    repository: RoomRepository,
) : RoomStateGetRepository(repository, RoomType.BOT), RoomGetRepository

@Singleton
class RoomTwoGetRepository @Inject constructor(
    repository: RoomRepository,
) : RoomStateGetRepository(repository, RoomType.TWO_PLAYERS), RoomGetRepository
