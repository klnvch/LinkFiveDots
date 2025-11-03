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

package by.klnvch.link5dots.bot

import by.klnvch.link5dots.domain.models.BotUser
import by.klnvch.link5dots.domain.models.DeviceOwnerUser
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.Room
import by.klnvch.link5dots.domain.models.dotModule
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.StringProvider
import by.klnvch.link5dots.domain.usecases.GameActionsBotUseCase
import by.klnvch.link5dots.ui.game.GameViewState
import by.klnvch.link5dots.ui.game.createGameViewState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun mapToBotGameViewState(
    dotsStyleType: DotsStyleType,
    userName: String?,
    stringProvider: StringProvider,
    room: IRoom?,
): GameViewState {
    val user1Name = userName ?: stringProvider.unknownName
    val user2Name = stringProvider.botName

    val gameActionsOnlineUseCase = GameActionsBotUseCase(object : RoomGetRepository {
        override val room = room
    })

    return createGameViewState(
        dotsStyleType,
        user1Name,
        user2Name,
        room,
        gameActionsOnlineUseCase.newAction,
        gameActionsOnlineUseCase.undoAction,
        gameActionsOnlineUseCase.shareAction,
    )
}

private val json = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
        polymorphic(IRoom::class) {
            subclass(Room::class)
        }
        polymorphic(IUser::class) {
            subclass(BotUser::class)
            subclass(DeviceOwnerUser::class)
        }
    } + dotModule
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun roomToJson(room: IRoom) = json.encodeToString(room)

@OptIn(ExperimentalJsExport::class)
@JsExport
fun jsonToRoom(str: String): IRoom = json.decodeFromString<IRoom>(str)
