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
package by.klnvch.link5dots.di.game.info

import by.klnvch.link5dots.domain.usecases.AddDotInfoUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GameActionsInfoUseCase
import by.klnvch.link5dots.domain.usecases.GameActionsUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomInfoUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreBotEmptyUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveInfoUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import dagger.Binds
import dagger.Module

@Module
interface InfoGameRulesModule {
    @Binds
    fun bindGetRoomUseCase(impl: GetRoomInfoUseCase): GetRoomUseCase

    @Binds
    fun bindAddDotUseCase(impl: AddDotInfoUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveInfoUseCase): UndoMoveUseCase

    @Binds
    fun bindSaveScoreUseCase(impl: SaveScoreBotEmptyUseCase): SaveScoreUseCase

    @Binds
    fun bindGameActionsUseCase(impl: GameActionsInfoUseCase): GameActionsUseCase
}
