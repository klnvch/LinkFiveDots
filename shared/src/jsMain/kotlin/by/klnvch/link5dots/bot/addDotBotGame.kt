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

import by.klnvch.link5dots.data.TimeServiceImpl
import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.models.Point
import by.klnvch.link5dots.domain.models.bot.Bot
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.usecases.AddDotBotUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport
fun addDotBotGame(room: IRoom?, p: Point, onGameUpdate: (room: IRoom) -> Unit): Promise<Unit> =
    GlobalScope.promise {
        val timeService = TimeServiceImpl()
        val board = Board()
        val bot = Bot(board)

        val getRepository = object : RoomGetRepository {
            override val room = room
        }

        val saveRepository = object : RoomSaveLocalRepository {
            override suspend fun save(room: IRoom) = onGameUpdate(room)
        }

        val useCase = AddDotBotUseCase(getRepository, board, timeService, saveRepository, bot)
        useCase.addDot(p)
    }
