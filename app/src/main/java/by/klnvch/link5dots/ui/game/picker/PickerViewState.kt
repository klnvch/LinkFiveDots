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
        state.scanState == ScanState.ON -> true
        state.scanState == ScanState.OFF -> true
        else -> false
    }

    val isScanButtonChecked = when (state.scanState) {
        ScanState.ON -> true
        else -> false
    }

    val scanProgressVisibility = when (state.scanState) {
        ScanState.ON -> View.VISIBLE
        else -> View.INVISIBLE
    }

    val isScanning = when (state.scanState) {
        ScanState.ON -> true
        else -> false
    }

    companion object {
        val INITIAL = PickerViewState(StateInitial())
        val IDLE = PickerViewState(StateIdle())
        val ERROR = PickerViewState(StateError())
        val TARGET_CREATING = PickerViewState(StateTargetCreating())
        val TARGET_DELETING = PickerViewState(StateTargetDeleting())
        fun connected(key: String) = PickerViewState(StateConnected(key))
        fun created(itemViewState: PickerItemViewState) =
                PickerViewState(StateTargetCreated(itemViewState))

        val START_SCAN = PickerViewState(StateScanning())
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
    val key = itemViewState.key
}

enum class ScanState {
    NONE, OFF, ON, DONE
}

sealed class ConnectState
object ConnectNone : ConnectState()
object ConnectConnecting : ConnectState()
class ConnectConnected(val key: String) : ConnectState()
object ConnectDisconnected : ConnectState()

sealed class GameState(
        val targetState: TargetState,
        val scanState: ScanState,
        val connectState: ConnectState,
)

private class StateInitial : GameState(TargetNone, ScanState.NONE, ConnectNone)
private class StateError : GameState(TargetNone, ScanState.NONE, ConnectNone)
private class StateIdle : GameState(TargetDeleted, ScanState.OFF, ConnectNone)
private class StateTargetCreating : GameState(TargetCreating, ScanState.NONE, ConnectNone)
class StateTargetCreated(itemViewState: PickerItemViewState) :
        GameState(TargetCreated(itemViewState), ScanState.NONE, ConnectNone)

private class StateTargetDeleting : GameState(TargetDeleting, ScanState.NONE, ConnectNone)
private class StateConnected(key: String) :
        GameState(TargetNone, ScanState.NONE, ConnectConnected(key))

private class StateScanning : GameState(TargetNone, ScanState.ON, ConnectNone)
private class StateConnecting : GameState(TargetNone, ScanState.NONE, ConnectConnecting)
private class StateDisconnected : GameState(TargetNone, ScanState.NONE, ConnectDisconnected)
