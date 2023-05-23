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
package by.klnvch.link5dots.ui.game.picker

import android.view.View
import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.ui.game.picker.adapters.PickerItemViewState

data class PickerViewState(private val state: GameState) {
    val targetState = state.targetState
    val scanState = state.scanState
    val connectState = state.connectState

    val isCreateButtonEnabled = when {
        state.connectState !is ConnectNone -> false
        state.targetState is TargetDeleted -> true
        state.targetState is TargetCreated -> true
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
        state.connectState !is ConnectNone -> false
        state.scanState is ScanOn -> true
        state.scanState == ScanOff -> true
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

    val isScanning = when (state.scanState) {
        is ScanOn -> true
        else -> false
    }

    val noResultLabelVisibility =
        if (state.scanState is ScanOn && state.scanState.items.isEmpty()) View.VISIBLE
        else View.GONE

    val discoveredItems = if (state.scanState is ScanOn) state.scanState.items else emptyList()

    companion object {
        val INITIAL = PickerViewState(StateInitial())
        val IDLE = PickerViewState(StateIdle())
        val ERROR = PickerViewState(StateError())
        val TARGET_CREATING = PickerViewState(StateTargetCreating())
        val TARGET_DELETING = PickerViewState(StateTargetDeleting())
        fun connected(descriptor: RemoteRoomDescriptor) =
            PickerViewState(StateConnected(descriptor))

        fun created(itemViewState: PickerItemViewState) =
            PickerViewState(StateTargetCreated(itemViewState))

        fun scanning(items: List<PickerItemViewState>) = PickerViewState(StateScanning(items))
        val CONNECTING = PickerViewState(StateConnecting())
        val DISCONNECTED = PickerViewState(StateDisconnected())
    }
}


sealed class TargetState
object TargetNone : TargetState()
object TargetDeleted : TargetState()
object TargetCreating : TargetState()
object TargetDeleting : TargetState()
class TargetCreated(val itemViewState: PickerItemViewState) : TargetState() {
    val descriptor = itemViewState.descriptor
}

sealed class ScanState
object ScanNone : ScanState()
object ScanOff : ScanState()
data class ScanOn(val items: List<PickerItemViewState>) : ScanState()
object ScanDone : ScanState()


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

private class StateTargetDeleting : GameState(TargetDeleting, ScanNone, ConnectNone)
private class StateConnected(descriptor: RemoteRoomDescriptor) :
    GameState(TargetNone, ScanNone, ConnectConnected(descriptor))

private class StateScanning(items: List<PickerItemViewState>) :
    GameState(TargetNone, ScanOn(items), ConnectNone)

private class StateConnecting : GameState(TargetNone, ScanNone, ConnectConnecting)
private class StateDisconnected : GameState(TargetNone, ScanNone, ConnectDisconnected)
