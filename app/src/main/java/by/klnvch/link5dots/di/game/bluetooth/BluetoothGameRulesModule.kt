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

package by.klnvch.link5dots.di.game.bluetooth

import by.klnvch.link5dots.domain.usecases.AddDotBluetoothUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomBluetoothUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.NewGameBluetoothUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.PrepareScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveInfoUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.BluetoothScanUseCase
import by.klnvch.link5dots.domain.usecases.network.ConnectBluetoothRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.ConnectRemoteRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateBluetoothRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.DeleteBluetoothRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.DeleteMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.GetBluetoothRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.GetMultiplayerRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.InitBluetoothUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import by.klnvch.link5dots.domain.usecases.network.UpdateBluetoothRoomStateUseCase
import by.klnvch.link5dots.domain.usecases.network.UpdateMultiplayerRoomStateUseCase
import dagger.Binds
import dagger.Module

@Module
interface BluetoothGameRulesModule {
    @Binds
    fun bindGetRoomUseCase(impl: GetRoomBluetoothUseCase): GetRoomUseCase

    @Binds
    fun bindNewGameUseCase(impl: NewGameBluetoothUseCase): NewGameUseCase

    @Binds
    fun bindAddDotUseCase(impl: AddDotBluetoothUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveInfoUseCase): UndoMoveUseCase

    @Binds
    fun bindPrepareScoreUseCase(impl: PrepareScoreMultiplayerUseCase): PrepareScoreUseCase

    @Binds
    fun bindInitMultiplayerUseCase(impl: InitBluetoothUseCase): InitMultiplayerUseCase

    @Binds
    fun bindCreateMultiplayerRoomUseCase(impl: CreateBluetoothRoomUseCase): CreateMultiplayerRoomUseCase

    @Binds
    fun bindGetMultiplayerRoomStateUseCase(impl: GetBluetoothRoomStateUseCase): GetMultiplayerRoomStateUseCase

    @Binds
    fun bindUpdateMultiplayerRoomStateUseCase(impl: UpdateBluetoothRoomStateUseCase): UpdateMultiplayerRoomStateUseCase

    @Binds
    fun bindScanUseCase(impl: BluetoothScanUseCase): ScanUseCase

    @Binds
    fun bindConnectRemoteRoomUseCase(impl: ConnectBluetoothRoomUseCase): ConnectRemoteRoomUseCase

    @Binds
    fun bindDeleteMultiplayerRoomUseCase(impl: DeleteBluetoothRoomUseCase): DeleteMultiplayerRoomUseCase
}
