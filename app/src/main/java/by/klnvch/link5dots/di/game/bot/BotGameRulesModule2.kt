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
package by.klnvch.link5dots.di.game.bot

import by.klnvch.link5dots.domain.models.Board
import by.klnvch.link5dots.domain.models.RoomBotFactory
import by.klnvch.link5dots.domain.models.RoomFactory
import by.klnvch.link5dots.domain.models.bot.Bot
import by.klnvch.link5dots.domain.repositories.RoomGetRepository
import by.klnvch.link5dots.domain.repositories.RoomKeyGenerator
import by.klnvch.link5dots.domain.repositories.RoomSaveLocalRepository
import by.klnvch.link5dots.domain.repositories.TimeService
import by.klnvch.link5dots.domain.usecases.AddDotBotUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GameActionsBotUseCase
import by.klnvch.link5dots.domain.usecases.GameActionsUseCase
import by.klnvch.link5dots.domain.usecases.NewGameCommonUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveBotUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import dagger.Module
import dagger.Provides

@Module
class BotGameRulesModule2 {
    @Provides
    fun provideBot(board: Board) = Bot(board)

    @Provides
    fun provideRoomBotFactory(
        roomKeyGenerator: RoomKeyGenerator,
        timeService: TimeService,
    ): RoomFactory = RoomBotFactory(roomKeyGenerator, timeService)

    @Provides
    fun provideNewGameUseCase(
        roomFactory: RoomFactory,
        saveRepository: RoomSaveLocalRepository,
    ): NewGameUseCase = NewGameCommonUseCase(roomFactory, saveRepository)

    @Provides
    fun provideAddDotUseCase(
        roomGetRepository: RoomGetRepository,
        board: Board,
        timeService: TimeService,
        roomSaveRepository: RoomSaveLocalRepository,
        bot: Bot,
    ): AddDotUseCase =
        AddDotBotUseCase(roomGetRepository, board, timeService, roomSaveRepository, bot)

    @Provides
    fun provideUndoMoveUseCase(
        roomGetRepository: RoomGetRepository,
        roomSaveRepository: RoomSaveLocalRepository,
    ): UndoMoveUseCase = UndoMoveBotUseCase(roomGetRepository, roomSaveRepository)

    @Provides
    fun provideGameActionsUseCase(roomGetRepository: RoomGetRepository): GameActionsUseCase =
        GameActionsBotUseCase(roomGetRepository)
}
