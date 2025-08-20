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
import by.klnvch.link5dots.domain.models.NetworkGameAction
import by.klnvch.link5dots.domain.repositories.NetworkUserIdentity
import by.klnvch.link5dots.ui.game.picker.states.PickerState

class GetNetworkGameActionUseCase(private val identity: NetworkUserIdentity) {
    suspend fun get(pickerState: PickerState, room: INetworkRoom?) = when {
        pickerState.isNone -> NetworkGameAction.DEFAULT
        pickerState.isCreating -> NetworkGameAction.PICKER_CREATING
        pickerState.isDeleting -> NetworkGameAction.PICKER_DELETING
        pickerState.isCreated -> NetworkGameAction.PICKER_CREATED
        pickerState.isScanning -> NetworkGameAction.PICKER_SCANNING
        pickerState.isConnecting -> NetworkGameAction.PICKER_CONNECTING
        room.isWon(identity.getUserId()) -> NetworkGameAction.GAME_OVER_WIN
        room.isLost(identity.getUserId()) -> NetworkGameAction.GAME_OVER_LOSE
        pickerState.isDisconnected -> NetworkGameAction.GAME_DISCONNECTED
        room.isMove(identity.getUserId()) -> NetworkGameAction.GAME_MOVE
        room.isWait(identity.getUserId()) -> NetworkGameAction.GAME_WAIT
        else -> NetworkGameAction.DEFAULT
    }
}

private fun INetworkRoom.canMove(userId: String) = when {
    user1.id == userId -> dots.size % 2 == 0
    user2?.id == userId -> dots.size % 2 == 1
    else -> null
}

private fun INetworkRoom?.isMove(userId: String) =
    this !== null && canMove(userId) == true

private fun INetworkRoom?.isWait(userId: String) =
    this !== null && canMove(userId) == false

private fun INetworkRoom?.isWon(userId: String) =
    this !== null && isOver() && canMove(userId) == false

private fun INetworkRoom?.isLost(userId: String) =
    this !== null && isOver() && canMove(userId) == true
