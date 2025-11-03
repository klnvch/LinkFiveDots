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

package by.klnvch.link5dots.data.db

import by.klnvch.link5dots.domain.models.BotUser
import by.klnvch.link5dots.domain.models.DeviceOwnerUser
import by.klnvch.link5dots.domain.models.HistoryRoom
import by.klnvch.link5dots.domain.models.HistoryRoomImpl
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.NetworkUser
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.domain.models.createNetworkUser

fun RoomLocal.mapToRoom(): IRoom = Room(
    key,
    timestamp.toInt(),
    dots?.toMutableList() ?: mutableListOf(),
    user1?.mapToUser(),
    user2?.mapToUser(),
)

fun RoomLocal.mapToHistoryRoom(): HistoryRoom = HistoryRoomImpl(
    key,
    timestamp.toInt(),
    dots?.toMutableList() ?: mutableListOf(),
    user1?.mapToUser(),
    user2?.mapToUser(),
    type.mapToRoomType(),
)

fun IRoom.mapToDbEntity(type: RoomType) = RoomLocal(
    key,
    time.toLong(),
    dots.toList(),
    user1?.mapToDbEntity(),
    user2?.mapToDbEntity(),
    type.mapToDbValue(),
    false,
    -1,
    false,
)

private fun IUser.mapToDbEntity() = when (this) {
    is BotUser -> UserLocal("bot", null)
    is DeviceOwnerUser -> UserLocal("host", null)
    is NetworkUser -> UserLocal(id, name)
}

private fun UserLocal.mapToUser(): IUser? = when (id) {
    "bot" -> BotUser
    "host" -> DeviceOwnerUser
    null -> null
    else -> createNetworkUser(id, name)
}

fun RoomType.mapToDbValue() = when (this) {
    RoomType.BLUETOOTH -> 1
    RoomType.NSD -> 2
    RoomType.ONLINE -> 3
    RoomType.TWO_PLAYERS -> 4
    RoomType.BOT -> 5
}

private fun Int.mapToRoomType() = when (this) {
    1 -> RoomType.BLUETOOTH
    2 -> RoomType.NSD
    3 -> RoomType.ONLINE
    4 -> RoomType.TWO_PLAYERS
    5 -> RoomType.BOT
    else -> RoomType.TWO_PLAYERS
}
