package by.klnvch.link5dots.data.network

import by.klnvch.link5dots.domain.models.*
import javax.inject.Inject

class RoomRemoteMapper @Inject constructor() {
    fun map(room: IRoom, isTest: Boolean): RoomRemote {
        return RoomRemote(
            room.timestamp,
            room.dots,
            map(room.user1),
            map(room.user2),
            room.type,
            isTest,
        )
    }

    private fun map(user: IUser?) = when (user) {
        is BotUser -> UserRemote("bot", null)
        is DeviceOwnerUser -> UserRemote("host", null)
        is NetworkUser -> UserRemote("", user.name)
        else -> null
    }
}
