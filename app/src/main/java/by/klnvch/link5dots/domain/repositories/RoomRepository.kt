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

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RoomType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

interface RoomRepository : RoomSaveRepository {
    suspend fun sync(isTestDevice: Boolean)
    suspend fun delete(room: IRoom)
    fun getAll(): Flow<List<IRoom>>
    fun getByKey(key: String): Flow<IRoom?>
    fun getRecentByType(type: RoomType): Flow<IRoom?>
    suspend fun deleteAll()
}

class RoomBotGetRepository @Inject constructor(
    private val repository: RoomRepository,
) : RoomGetRepository {
    override suspend fun get() = repository.getRecentByType(RoomType.BOT).firstOrNull()
}

class RoomTwoGetRepository @Inject constructor(
    private val repository: RoomRepository,
) : RoomGetRepository {
    override suspend fun get() = repository.getRecentByType(RoomType.TWO_PLAYERS).firstOrNull()
}
