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

package by.klnvch.link5dots.online

import by.klnvch.link5dots.data.online.mapper.toOnlineRoom
import by.klnvch.link5dots.data.online.models.OnlineDotRemote
import by.klnvch.link5dots.data.online.models.OnlineRemoteUser
import by.klnvch.link5dots.data.online.models.OnlineRoom
import by.klnvch.link5dots.data.online.models.OnlineRoomRemote
import by.klnvch.link5dots.data.online.models.RemoteRoomItem
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.GameState
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.usecases.GameActionsOnlineUseCase
import by.klnvch.link5dots.ui.game.GameViewState
import by.klnvch.link5dots.ui.game.createGameViewState
import kotlinx.coroutines.DelicateCoroutinesApi

fun parseTime(value: dynamic) = (value as Double?)?.toLong()
fun parseUser(value: dynamic) = if (value != null) OnlineRemoteUser(value.id, value.name) else null

@OptIn(ExperimentalJsExport::class, ExperimentalJsCollectionsApi::class)
@JsExport
fun toOnlineRoom(key: String?, value: dynamic?): OnlineRoom {
    val value = OnlineRoomRemote(
        parseTime(value.time),
        (value.dots as Array<dynamic>?)
            ?.toList()
            ?.filterNotNull()
            ?.map { OnlineDotRemote(parseTime(it.t), it.x as Int?, it.y as Int?) },
        parseUser(value.user1),
        parseUser(value.user2),
        value.state as Int?,
    )
    val item = RemoteRoomItem(key, value)
    return item.toOnlineRoom()
}

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun mapToGameViewState(
    dotsStyleType: DotsStyleType,
    defaultName: String,
    room: INetworkRoom,
): GameViewState {
    val user1Name = room.user1.name ?: defaultName
    val user2Name = room.user2.name ?: defaultName

    val gameActionsOnlineUseCase = GameActionsOnlineUseCase(object : RoomRemoteRepository {
        override var room = room
    })

    val gameState = GameState(
        room,
        user1Name,
        user2Name,
        true,
    )

    return createGameViewState(
        dotsStyleType,
        gameState,
        gameActionsOnlineUseCase.newAction,
        gameActionsOnlineUseCase.undoAction,
        gameActionsOnlineUseCase.shareAction,
    )
}
