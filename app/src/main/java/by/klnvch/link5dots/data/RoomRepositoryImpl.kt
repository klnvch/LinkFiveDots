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

package by.klnvch.link5dots.data

import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.data.db.RoomDao
import by.klnvch.link5dots.data.db.RoomLocalMapper
import by.klnvch.link5dots.data.network.NetworkService
import by.klnvch.link5dots.data.network.RoomRemoteMapper
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.repositories.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val roomLocalSource: RoomDao,
    private val roomLocalMapper: RoomLocalMapper,
    private val roomRemoteSource: NetworkService,
    private val roomRemoteMapper: RoomRemoteMapper,
) : RoomRepository {
    override suspend fun sync(isTestDevice: Boolean) = roomLocalSource
        .getNotSent()
        .map { roomLocalMapper.map(it) }
        .forEach {
            roomRemoteSource.addRoom(HISTORY_TABLE, it.key, roomRemoteMapper.map(it, isTestDevice))
            roomLocalSource.setSent(it.key)
        }

    override suspend fun save(room: IRoom) = roomLocalSource
        .insert(roomLocalMapper.map(room))

    override suspend fun delete(room: IRoom) = roomLocalSource
        .delete(roomLocalMapper.map(room))

    override fun getAll(): Flow<List<IRoom>> = roomLocalSource
        .getAll()
        .map { list -> list.map { roomLocalMapper.map(it) } }

    override suspend fun getByKey(key: String) = roomLocalSource
        .getByKey(key)
        .map { roomLocalMapper.map(it) }
        .firstOrNull()

    override suspend fun getRecentByType(type: Int) = roomLocalSource
        .getRecentByType(type)
        .map { roomLocalMapper.map(it) }
        .firstOrNull()

    override suspend fun deleteAll() = roomLocalSource.deleteAll()

    companion object {
        private val HISTORY_TABLE = if (BuildConfig.DEBUG) "history_debug" else "history"
    }
}
