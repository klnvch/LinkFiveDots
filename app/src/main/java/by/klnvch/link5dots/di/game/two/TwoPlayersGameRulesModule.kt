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
package by.klnvch.link5dots.di.game.two

import by.klnvch.link5dots.domain.usecases.AddDotTwoUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomOfflineUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.NewGameTwoUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreOtherUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveTwoUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import dagger.Binds
import dagger.Module

@Module
interface TwoPlayersGameRulesModule {
    @Binds
    fun bindGetRoomUseCase(impl: GetRoomOfflineUseCase): GetRoomUseCase

    @Binds
    fun bindNewGameUseCase(impl: NewGameTwoUseCase): NewGameUseCase

    @Binds
    fun bindAddDotUseCase(impl: AddDotTwoUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveTwoUseCase): UndoMoveUseCase

    @Binds
    fun bindPrepareScoreUseCase(impl: PrepareScoreOtherUseCase): PrepareScoreUseCase
}
