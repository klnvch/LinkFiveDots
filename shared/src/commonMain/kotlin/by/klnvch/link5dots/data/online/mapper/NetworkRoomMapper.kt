/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.data.online.mapper

import by.klnvch.link5dots.data.online.models.OnlineDotRemote
import by.klnvch.link5dots.data.online.models.OnlineRemoteUser
import by.klnvch.link5dots.data.online.models.OnlineRoomInvitationRemote
import by.klnvch.link5dots.data.online.models.OnlineRoomRemote
import by.klnvch.link5dots.domain.models.Dot
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.INetworkRoomInvitation
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.models.NetworkRoomInvitation
import by.klnvch.link5dots.domain.models.createDot
import by.klnvch.link5dots.domain.models.createNetworkUser

private class ParseException(entity: String) : Exception("Failed to parse $entity")

private fun OnlineRemoteUser?.parseUser() = createNetworkUser(this?.id, this?.name)
    ?: throw ParseException("user")

private fun Long?.parseTime() = this?.let { it / 1000 }?.toInt()
    ?: throw ParseException("time")

private fun Long?.parseDt(time: Int) = this?.let { (it / 1000).toInt() - time } ?: 0
private fun OnlineDotRemote?.parseDot(time: Int): Dot? = this?.let {
    if (it.x != null && it.y != null) createDot(it.x, it.y, it.t.parseDt(time)) else null
}

private fun List<OnlineDotRemote?>?.toDots(time: Int) =
    this?.mapNotNull { it.parseDot(time) } ?: emptyList()

fun OnlineRoomInvitationRemote.toInvitation(key: String): INetworkRoomInvitation =
    NetworkRoomInvitation(key, time.parseTime(), user1.parseUser())

fun OnlineRoomRemote.toRoom(key: String): INetworkRoom {
    val time = time.parseTime()
    return NetworkRoom(
        key,
        time,
        dots.toDots(time),
        user1.parseUser(),
        user2.parseUser(),
    )
}
