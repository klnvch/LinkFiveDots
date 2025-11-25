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

package by.klnvch.link5dots.di.game

import by.klnvch.link5dots.domain.repositories.GameActionsFactory
import by.klnvch.link5dots.domain.repositories.GameActionsSocketFactory
import by.klnvch.link5dots.domain.repositories.NetworkUserLocalProvider
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.domain.repositories.RoomCleanRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomFlowRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomRemoteRepository
import by.klnvch.link5dots.domain.repositories.RoomStateRemoteRepository
import by.klnvch.link5dots.domain.repositories.SocketCreateRepository
import by.klnvch.link5dots.domain.repositories.SocketRoomInvitationRepository
import by.klnvch.link5dots.domain.repositories.SocketRoomRepository
import by.klnvch.link5dots.domain.repositories.SocketSendRepository
import by.klnvch.link5dots.domain.usecases.AddDotSocketUseCase
import by.klnvch.link5dots.domain.usecases.AddDotUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomSocketUseCase
import by.klnvch.link5dots.domain.usecases.GetRoomUseCase
import by.klnvch.link5dots.domain.usecases.NewGameSocketUseCase
import by.klnvch.link5dots.domain.usecases.NewGameUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreEmptyUseCase
import by.klnvch.link5dots.domain.usecases.SaveScoreUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveSocketUseCase
import by.klnvch.link5dots.domain.usecases.UndoMoveUseCase
import by.klnvch.link5dots.domain.usecases.network.CommonScanUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateMultiplayerRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.CreateSocketRoomUseCase
import by.klnvch.link5dots.domain.usecases.network.ScanUseCase
import dagger.Binds
import dagger.Module

@Module
interface SocketGameRulesModule {
    @Binds
    fun bindGetRoomUseCase(impl: GetRoomSocketUseCase): GetRoomUseCase

    @Binds
    fun bindNewGameUseCase(impl: NewGameSocketUseCase): NewGameUseCase

    @Binds
    fun bindAddDotUseCase(impl: AddDotSocketUseCase): AddDotUseCase

    @Binds
    fun bindUndoMoveUseCase(impl: UndoMoveSocketUseCase): UndoMoveUseCase

    @Binds
    fun bindSaveScoreUseCase(impl: SaveScoreEmptyUseCase): SaveScoreUseCase

    @Binds
    fun bindCreateMultiplayerRoomUseCase(impl: CreateSocketRoomUseCase): CreateMultiplayerRoomUseCase

    @Binds
    fun bindScanUseCase(impl: CommonScanUseCase): ScanUseCase

    @Binds
    fun bindNetworkUserProvider(impl: NetworkUserLocalProvider): NetworkUserProvider

    @Binds
    fun bindGameActionsFactory(impl: GameActionsSocketFactory): GameActionsFactory

    @Binds
    fun bindSocketGetFlowRepository(impl: SocketRoomRepository): RoomFlowRemoteRepository

    @Binds
    fun bindNetworkRoomStateRepository(impl: SocketRoomRepository): RoomStateRemoteRepository

    @Binds
    fun bindRoomCleanRemoteRepository(impl: SocketRoomRepository): RoomCleanRemoteRepository

    @Binds
    fun bindSocketGetRepository(impl: SocketRoomRepository): RoomRemoteRepository

    @Binds
    fun bindSocketCreateRepository(impl: SocketRoomRepository): SocketCreateRepository

    @Binds
    fun bindSocketSendRepository(impl: SocketRoomRepository): SocketSendRepository

    @Binds
    fun bindSocketRoomInvitationRepository(impl: SocketRoomRepository): SocketRoomInvitationRepository
}
