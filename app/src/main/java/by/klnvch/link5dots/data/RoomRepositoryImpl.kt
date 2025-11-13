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

package by.klnvch.link5dots.data

import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.db.RoomDao
import by.klnvch.link5dots.data.db.mapToDbEntity
import by.klnvch.link5dots.data.db.mapToDbValue
import by.klnvch.link5dots.data.db.mapToHistoryRoom
import by.klnvch.link5dots.data.db.mapToRoom
import by.klnvch.link5dots.data.network.NetworkService
import by.klnvch.link5dots.data.network.RoomRemoteMapper
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.repositories.RoomRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomLocalSource: RoomDao,
    private val roomRemoteSource: NetworkService,
    private val roomRemoteMapper: RoomRemoteMapper,
) : RoomRepository {
    override suspend fun sync(isTestDevice: Boolean) = roomLocalSource
        .getNotSent()
        .map { it.mapToHistoryRoom() }
        .forEach {
            roomRemoteSource.addRoom(HISTORY_TABLE, it.key, roomRemoteMapper.map(it, isTestDevice))
            roomLocalSource.setSent(it.key)
        }

    override suspend fun save(room: IRoom, roomType: RoomType) = roomLocalSource
        .insert(room.mapToDbEntity(roomType))

    override suspend fun delete(key: String) = roomLocalSource.deleteByKey(key)

    override fun getAll(types: List<RoomType>) = roomLocalSource
        .getAll(types.map { it.mapToDbValue() })
        .map { list -> list.map { it.mapToHistoryRoom() } }

    override fun getByKey(key: String) = roomLocalSource
        .getByKey(key)
        .map { it.firstOrNull() }
        .map { it?.mapToRoom() }

    override fun getRecentByType(type: RoomType) = roomLocalSource
        .getRecentByType(type.mapToDbValue())
        .map { it.firstOrNull() }
        .map { it?.mapToRoom() }

    override suspend fun deleteAll() = roomLocalSource.deleteAll()

    companion object {
        private val HISTORY_TABLE = if (BuildConfig.DEBUG) "history_debug" else "history"
    }
}
