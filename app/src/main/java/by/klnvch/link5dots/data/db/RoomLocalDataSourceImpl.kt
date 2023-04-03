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

package by.klnvch.link5dots.data.db

import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.repositories.RoomLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomLocalDataSourceImpl @Inject constructor(
    private val roomDao: RoomDao,
    private val mapper: RoomLocalMapper,
) : RoomLocalDataSource {
    override suspend fun save(room: IRoom) {
        roomDao.insert(mapper.map(room))
    }

    override suspend fun delete(room: IRoom) {
        roomDao.deleteById(room.key)
    }

    override suspend fun getNotSent(): List<IRoom> {
        return roomDao.getNotSent().map { mapper.map(it) }
    }

    override suspend fun setSent(room: IRoom) {
        roomDao.setSent(room.key)
    }

    override suspend fun getRecentByType(type: Int): List<IRoom> {
        return roomDao.getRecentByType(type).map { mapper.map(it) }
    }

    override suspend fun getByKey(key: String): IRoom? {
        return roomDao.getByKey(key).map { mapper.map(it) }.firstOrNull()
    }

    override fun getAll(): Flow<List<IRoom>> {
        return roomDao.getAll().map { list -> list.map { mapper.map(it) } }
    }
}
