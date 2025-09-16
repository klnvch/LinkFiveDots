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

package by.klnvch.link5dots

import by.klnvch.link5dots.data.firebase.RemoteRoomItem
import by.klnvch.link5dots.data.online.mapToDescriptors
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.NetworkRoom
import by.klnvch.link5dots.domain.repositories.GetOnlineRoomRepository
import by.klnvch.link5dots.domain.usecases.GameActionsOnlineUseCase
import by.klnvch.link5dots.ui.game.GameViewState
import by.klnvch.link5dots.ui.game.createGameViewState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.js.collections.JsReadonlyArray
import kotlin.js.collections.toList

@OptIn(ExperimentalJsExport::class, ExperimentalJsCollectionsApi::class)
@JsExport()
fun mapToDescriptors(items: JsReadonlyArray<RemoteRoomItem>, defaultName: String) =
    mapToDescriptors(items.toList(), defaultName).toTypedArray()

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport()
fun mapToGameViewState(
    defaultName: String,
    room: NetworkRoom,
): GameViewState {
    val user1Name = room.user1.name ?: defaultName
    val user2Name = room.user2?.name ?: defaultName

    val gameActionsOnlineUseCase = GameActionsOnlineUseCase(object : GetOnlineRoomRepository {
        override var room: NetworkRoom? = room
    })

    return createGameViewState(
        DotsStyleType.ORIGINAL,
        user1Name,
        user2Name,
        room,
        gameActionsOnlineUseCase.newAction,
        gameActionsOnlineUseCase.undoAction,
        gameActionsOnlineUseCase.shareAction,
    )
}
