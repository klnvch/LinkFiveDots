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

import by.klnvch.link5dots.domain.models.*
import javax.inject.Inject

class RoomLocalMapper @Inject constructor() {
    fun map(room: IRoom) = RoomLocal(
        room.key,
        room.timestamp,
        room.dots,
        map(room.user1),
        map(room.user2),
        room.type,
        false,
        -1,
        false,
    )

    fun map(local: RoomLocal): IRoom = Room(
        local.key,
        local.timestamp,
        local.dots?.toMutableList() ?: mutableListOf(),
        map(local.user1),
        map(local.user2),
        local.type
    )

    private fun map(user: IUser?) = when (user) {
        is BotUser -> UserLocal("bot", null)
        is DeviceOwnerUser -> UserLocal("host", null)
        is NetworkUser -> UserLocal("", user.name)
        else -> null
    }

    private fun map(user: UserLocal?): IUser? = when (user?.id) {
        "bot" -> BotUser
        "host" -> DeviceOwnerUser
        null -> null
        else -> if (user.name.isNullOrEmpty()) null else NetworkUser(user.id, user.name)
    }
}
