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

package by.klnvch.link5dots.domain.usecases.network

import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.IUser
import by.klnvch.link5dots.domain.models.NetworkGameAction
import by.klnvch.link5dots.domain.repositories.NetworkUserProvider
import by.klnvch.link5dots.ui.game.picker.states.PickerState

class GetNetworkGameActionUseCase(private val networkUserProvider: NetworkUserProvider) {
    fun get(pickerState: PickerState, room: INetworkRoom?) = when {
        pickerState.isNone -> NetworkGameAction.DEFAULT
        pickerState.isCreating -> NetworkGameAction.PICKER_CREATING
        pickerState.isDeleting -> NetworkGameAction.PICKER_DELETING
        pickerState.isCreated -> NetworkGameAction.PICKER_CREATED
        pickerState.isScanning -> NetworkGameAction.PICKER_SCANNING
        pickerState.isConnecting -> NetworkGameAction.PICKER_CONNECTING
        room.isWon(networkUserProvider.networkUser) -> NetworkGameAction.GAME_OVER_WIN
        room.isLost(networkUserProvider.networkUser) -> NetworkGameAction.GAME_OVER_LOSE
        pickerState.isDisconnected -> NetworkGameAction.GAME_DISCONNECTED
        room.isMove(networkUserProvider.networkUser) -> NetworkGameAction.GAME_MOVE
        room.isWait(networkUserProvider.networkUser) -> NetworkGameAction.GAME_WAIT
        else -> NetworkGameAction.DEFAULT
    }
}

private fun INetworkRoom.canMove(user: IUser?) = when {
    user1 == user -> dots.size % 2 == 0
    user2 == user -> dots.size % 2 == 1
    else -> null
}

private fun INetworkRoom?.isMove(user: IUser?) =
    this !== null && canMove(user) == true

private fun INetworkRoom?.isWait(user: IUser?) =
    this !== null && canMove(user) == false

private fun INetworkRoom?.isWon(user: IUser?) =
    this !== null && isOver() && canMove(user) == false

private fun INetworkRoom?.isLost(user: IUser?) =
    this !== null && isOver() && canMove(user) == true
