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
package by.klnvch.link5dots.di.game.online

import by.klnvch.link5dots.data.FirebaseUserIdentity
import by.klnvch.link5dots.domain.repositories.NetworkUserIdentity
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomOnlineUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.NewGameOnlineUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveInfoUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.DeleteMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.DeleteOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.GetNetworkRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.GetOnlineRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.InitOnlineUseCase
import by.klnvch.link5dots.domain.usecases.network.OnlineScanUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import dagger.Binds
import dagger.Module

@Module
interface OnlineGameRulesModule {
    @Binds
    fun bindGetRoomUseCase(impl: GetRoomOnlineUseCase): GetRoomUseCase

    @Binds
    fun bindGetMultiplayerRoomStateUseCase(impl: GetOnlineRoomStateUseCase): GetNetworkRoomStateUseCase

    @Binds
    fun bindNewGameUseCase(impl: NewGameOnlineUseCase): NewGameUseCase

    @Binds
    fun bindAddDotUseCase(impl: AddDotOnlineUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveInfoUseCase): UndoMoveUseCase

    @Binds
    fun bindPrepareScoreUseCase(impl: PrepareScoreMultiplayerUseCase): PrepareScoreUseCase

    @Binds
    fun bindInitMultiplayerUseCase(impl: InitOnlineUseCase): InitMultiplayerUseCase

    @Binds
    fun bindCreateMultiplayerRoomUseCase(impl: CreateOnlineRoomUseCase): CreateMultiplayerRoomUseCase

    @Binds
    fun bindScanUseCase(impl: OnlineScanUseCase): ScanUseCase

    @Binds
    fun bindDeleteMultiplayerRoomUseCase(impl: DeleteOnlineRoomUseCase): DeleteMultiplayerRoomUseCase

    @Binds
    fun bindNetworkUserIdentity(impl: FirebaseUserIdentity): NetworkUserIdentity
}
