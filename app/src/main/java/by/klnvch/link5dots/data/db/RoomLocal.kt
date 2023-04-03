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

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import by.klnvch.link5dots.domain.models.Dot

data class UserLocal(
    @ColumnInfo(name = "id") val id: String?,
    @ColumnInfo(name = "name") val name: String?
)

@Entity(tableName = "rooms")
data class RoomLocal(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "dots") val dots: List<Dot>?,
    @Embedded(prefix = "user_1_") val user1: UserLocal?,
    @Embedded(prefix = "user_2_") val user2: UserLocal?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "is_send") val isSend: Boolean,
    @ColumnInfo(name = "state") val _removed_1: Int,
    @ColumnInfo(name = "is_test") val _removed_2: Boolean,
)
