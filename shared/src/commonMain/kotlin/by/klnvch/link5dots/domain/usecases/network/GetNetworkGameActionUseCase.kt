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
import by.klnvch.link5dots.domain.models.RoomState
import by.klnvch.link5dots.domain.repositories.NetworkUserIdentity
import by.klnvch.link5dots.ui.game.picker.states.PickerState

class GetNetworkGameActionUseCase(private val identity: NetworkUserIdentity) {
    suspend fun get(pickerState: PickerState, room: INetworkRoom?) = when {
        pickerState.isCreating -> NetworkGameAction.PICKER_CREATING
        pickerState.isDeleting -> NetworkGameAction.PICKER_DELETING
        pickerState.isConnected && room != null -> get(room)
        pickerState.isCreated -> NetworkGameAction.PICKER_CREATED
        pickerState.isScanning -> NetworkGameAction.PICKER_SCANNING
        pickerState.isConnecting -> NetworkGameAction.PICKER_CONNECTING
        else -> NetworkGameAction.UNKNOWN
    }

    private suspend fun get(room: INetworkRoom): NetworkGameAction {
        val userId = identity.getUserId()
        return if (room.user1.id == userId) map(room, 0)
        else if (room.user2?.id == userId) map(room, 1)
        else NetworkGameAction.UNKNOWN
    }

    private fun map(room: INetworkRoom, expectedOrder: Int): NetworkGameAction {
        val order = room.dots.size % 2
        return if (room.isOver())
            if (order == expectedOrder) NetworkGameAction.GAME_OVER_LOSE
            else NetworkGameAction.GAME_OVER_WIN
        else if (room.state == RoomState.FINISHED) NetworkGameAction.GAME_DISCONNECTED
        else if (order == expectedOrder) NetworkGameAction.GAME_MOVE
        else NetworkGameAction.GAME_WAIT
    }
}
