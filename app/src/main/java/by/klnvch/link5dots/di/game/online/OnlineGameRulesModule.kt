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

import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.data.online.OnlineRoomRepositoryImpl
import by.klnvch.link5dots.di.viewmodels.ViewModelKey
import by.klnvch.link5dots.domain.models.RoomTypeOnlineProvider
import by.klnvch.link5dots.domain.models.RoomTypeProvider
import by.klnvch.link5dots.domain.repositories.NetworkUserFirebaseProvider
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.OnlineRoomRepository
import by.klnvch.link5dots.domain.repositories.RoomCleanRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowOnlineRepository
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomStateRemoteRepository
import by.klnvch.link5dots.domain.usecases.AddDotOnlineUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomOnlineUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreEmptyUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveEmptyUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateOnlineRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.InitMultiplayerUseCase
import by.klnvch.link5dots.domain.usecases.network.InitOnlineUseCase
import by.klnvch.link5dots.domain.usecases.network.OnlineScanUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import by.klnvch.link5dots.ui.game.picker.FirebaseStatusViewModel
import by.klnvch.link5dots.ui.game.viewmodels.OnlineUserHistoryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface OnlineGameRulesModule {
    @Binds
    fun bindGetRoomUseCase(impl: GetRoomOnlineUseCase): GetRoomUseCase

    @Binds
    fun bindRoomTypeProvider(impl: RoomTypeOnlineProvider): RoomTypeProvider

    @Binds
    fun bindOnlineRoomRepository(impl: OnlineRoomRepositoryImpl): OnlineRoomRepository

    @Binds
    fun bindNetworkRoomStateRepository(impl: OnlineRoomRepository): RoomStateRemoteRepository

    @Binds
    fun bindAddDotUseCase(impl: AddDotOnlineUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveEmptyUseCase): UndoMoveUseCase

    @Binds
    fun bindSaveScoreUseCase(impl: SaveScoreEmptyUseCase): SaveScoreUseCase

    @Binds
    fun bindInitMultiplayerUseCase(impl: InitOnlineUseCase): InitMultiplayerUseCase

    @Binds
    fun bindCreateMultiplayerRoomUseCase(impl: CreateOnlineRoomUseCase): CreateMultiplayerRoomUseCase

    @Binds
    fun bindScanUseCase(impl: OnlineScanUseCase): ScanUseCase

    @Binds
    fun bindRoomCleanRemoteRepository(impl: OnlineRoomRepository): RoomCleanRemoteRepository

    @Binds
    fun bindNetworkUserProvider(impl: NetworkUserFirebaseProvider): NetworkUserProvider

    @Binds
    fun bindRoomFlowOnlineRepository(impl: OnlineRoomRepository): RoomFlowOnlineRepository

    @Binds
    fun bindRoomRemoteRepository(impl: OnlineRoomRepository): RoomRemoteRepository

    @Binds
    @IntoMap
    @ViewModelKey(FirebaseStatusViewModel::class)
    fun bindFirebaseStatusViewModel(viewModel: FirebaseStatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnlineUserHistoryViewModel::class)
    fun bindOnlineUserHistoryViewModel(viewModel: OnlineUserHistoryViewModel): ViewModel
}
