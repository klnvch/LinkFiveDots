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

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: RoomLocal)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRoom(room: RoomLocal)

    @Delete
    suspend fun delete(room: RoomLocal)

    @Query("DELETE FROM rooms WHERE key = :key")
    suspend fun deleteById(key: String)

    @Query("SELECT * FROM rooms ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RoomLocal>>

    @Query("SELECT * FROM rooms WHERE is_send = 0 ORDER BY timestamp DESC")
    suspend fun getNotSent(): List<RoomLocal>

    @Query("UPDATE rooms SET is_send = 1 WHERE key = :key")
    suspend fun setSent(key: String)

    @Query("DELETE FROM rooms")
    suspend fun deleteAll()

    @Query("SELECT * FROM rooms WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    fun getRecentByType(type: Int): Flow<List<RoomLocal>>

    @Query("SELECT * FROM rooms WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): List<RoomLocal>
}
