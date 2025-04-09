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
package by.klnvch.link5dots.ui.game.picker

import android.view.View
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.ui.game.picker.adapters.PickerItemViewState
import by.klnvch.link5dots.ui.game.picker.states.ScanDone
import by.klnvch.link5dots.ui.game.picker.states.ScanFailed
import by.klnvch.link5dots.ui.game.picker.states.ScanNone
import by.klnvch.link5dots.ui.game.picker.states.ScanOff
import by.klnvch.link5dots.ui.game.picker.states.ScanOn
import by.klnvch.link5dots.ui.game.picker.states.ScanState
import by.klnvch.link5dots.ui.game.picker.states.TargetCreated
import by.klnvch.link5dots.ui.game.picker.states.TargetCreating
import by.klnvch.link5dots.ui.game.picker.states.TargetDeleted
import by.klnvch.link5dots.ui.game.picker.states.TargetDeleting
import by.klnvch.link5dots.ui.game.picker.states.TargetFailed
import by.klnvch.link5dots.ui.game.picker.states.TargetNone
import by.klnvch.link5dots.ui.game.picker.states.TargetState

data class PickerViewState(private val state: GameState) {
    val targetState = state.targetState
    val scanState = state.scanState
    val connectState = state.connectState

    val isCreateButtonEnabled = when {
        state.connectState is ConnectDisconnected -> true
        state.connectState !is ConnectNone -> false
        state.targetState is TargetDeleted -> true
        state.targetState is TargetCreated -> true
        state.targetState is TargetFailed -> true
        else -> false
    }

    val isCreateButtonChecked = when (state.targetState) {
        is TargetCreated -> true
        is TargetDeleting -> true
        else -> false
    }

    val progressCreateVisibility = when (state.targetState) {
        is TargetCreating -> View.VISIBLE
        is TargetDeleting -> View.VISIBLE
        else -> View.INVISIBLE
    }

    val statusVisibility = when (state.targetState) {
        is TargetCreating -> View.INVISIBLE
        is TargetDeleting -> View.INVISIBLE
        else -> View.VISIBLE
    }

    val isScanButtonEnabled = when {
        state.connectState is ConnectDisconnected -> true
        state.connectState !is ConnectNone -> false
        state.scanState is ScanOn -> true
        state.scanState is ScanOff -> true
        state.scanState is ScanDone -> true
        state.scanState is ScanFailed -> true
        else -> false
    }

    val isScanButtonChecked = when (state.scanState) {
        is ScanOn -> true
        else -> false
    }

    val scanProgressVisibility = when (state.scanState) {
        is ScanOn -> View.VISIBLE
        else -> View.INVISIBLE
    }

    val discoveredItems = when (state.scanState) {
        is ScanOn -> state.scanState.items
        is ScanDone -> state.scanState.items
        else -> emptyList()
    }

    companion object {
        val INITIAL = PickerViewState(StateInitial())
        val IDLE = PickerViewState(StateIdle())
        val ERROR = PickerViewState(StateError())
        val TARGET_CREATING = PickerViewState(StateTargetCreating())
        val TARGET_DELETING = PickerViewState(StateTargetDeleting())
        fun connected(descriptor: RemoteRoomDescriptor) =
            PickerViewState(StateConnected(descriptor))

        fun creationFailed(e: Throwable) = PickerViewState(StateTargetFailed(e))

        fun created(itemViewState: PickerItemViewState) =
            PickerViewState(StateTargetCreated(itemViewState))

        fun scanning(items: List<PickerItemViewState>) = PickerViewState(StateScanning(items))
        fun scanDone(items: List<PickerItemViewState>) = PickerViewState(StateScanDone(items))
        fun scanFailed(e: Throwable) = PickerViewState(StateScanFailed(e))

        val CONNECTING = PickerViewState(StateConnecting())
        val DISCONNECTED = PickerViewState(StateDisconnected())
    }
}

sealed class ConnectState
object ConnectNone : ConnectState()
object ConnectConnecting : ConnectState()
class ConnectConnected(val descriptor: RemoteRoomDescriptor) : ConnectState()
object ConnectDisconnected : ConnectState()

sealed class GameState(
    val targetState: TargetState,
    val scanState: ScanState,
    val connectState: ConnectState,
)

private class StateInitial : GameState(TargetNone, ScanNone, ConnectNone)
private class StateError : GameState(TargetNone, ScanNone, ConnectNone)
private class StateIdle : GameState(TargetDeleted, ScanOff, ConnectNone)
private class StateTargetCreating : GameState(TargetCreating, ScanNone, ConnectNone)
class StateTargetCreated(itemViewState: PickerItemViewState) :
    GameState(TargetCreated(itemViewState), ScanNone, ConnectNone)

class StateTargetFailed(e: Throwable) : GameState(TargetFailed(e), ScanOff, ConnectNone)

private class StateTargetDeleting : GameState(TargetDeleting, ScanNone, ConnectNone)
private class StateConnected(descriptor: RemoteRoomDescriptor) :
    GameState(TargetNone, ScanNone, ConnectConnected(descriptor))

private class StateScanning(items: List<PickerItemViewState>) :
    GameState(TargetNone, ScanOn(items), ConnectNone)

private class StateScanDone(items: List<PickerItemViewState>) :
    GameState(TargetDeleted, ScanDone(items), ConnectNone)

private class StateScanFailed(e: Throwable) :
    GameState(TargetDeleted, ScanFailed(e), ConnectNone)

private class StateConnecting : GameState(TargetNone, ScanNone, ConnectConnecting)
private class StateDisconnected : GameState(TargetNone, ScanNone, ConnectDisconnected)
