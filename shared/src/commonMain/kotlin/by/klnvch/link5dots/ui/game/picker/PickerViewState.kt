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

package by.klnvch.link5dots.ui.game.picker

import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import by.klnvch.link5dots.ui.game.picker.states.PickerState
import by.klnvch.link5dots.ui.game.picker.states.createInitialPickerState
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport()
sealed interface PickerScreen

@OptIn(ExperimentalJsExport::class)
@JsExport()
object PickerScreenNone : PickerScreen

@OptIn(ExperimentalJsExport::class)
@JsExport()
object PickerScreenGame : PickerScreen

@OptIn(ExperimentalJsExport::class)
@JsExport()
class PickerScreenError(val e: Throwable) : PickerScreen

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PickerViewState {
    val screen: PickerScreen
    val common: PickerCommonViewState
    val creation: PickerCreationViewState
    val scanning: PickerScanningViewState
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PickerCommonViewState {
    val inProgress: Boolean
    val msg: String?
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PickerCreationViewState {
    val targetName: String?
    val isEnabled: Boolean
    val isCreateButtonVisible: Boolean
    val isDeleteButtonVisible: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PickerScanningViewState {
    val isEnabled: Boolean
    val isStartScanButtonVisible: Boolean
    val isCancelScanButtonVisible: Boolean
    val discoveredItems: Array<PickerItemViewState>
    val isEmptyMessageVisible: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PickerItemViewState {
    val id: Int
    val descriptor: RemoteRoomDescriptor
    val shortName: String
    val longName: String
    val isBold: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
fun PickerState.toPickerViewState(): PickerViewState = PickerViewStateImpl(this)

data class PickerViewStateImpl(
    val state: PickerState = createInitialPickerState(),
) : PickerViewState {
    private val isConnected = state.isConnected
    private val error = state.error
    private val isTargetChanging = state.isCreating || state.isDeleting || state.isConnecting

    override val screen = when {
        isConnected -> PickerScreenGame
        error != null -> PickerScreenError(error)
        else -> PickerScreenNone
    }

    override val common = PickerCommonViewStateImpl(
        isTargetChanging || state.isScanning,
        state.error?.message
    )

    override val creation = PickerCreationViewStateImpl(
        state.result?.let { "${it.title} ${it.description}" },
        !isTargetChanging && !state.isScanning && !state.isConnected,
        !state.isCreated,
        state.isCreated,
    )

    override val scanning = PickerScanningViewStateImpl(
        !isTargetChanging && !state.isCreated && !state.isConnected,
        !state.isScanning,
        state.isScanning,
        state.scanResult.mapIndexed { id, d -> PickerItemViewStateImpl(id, d) }.toTypedArray(),
        state.isScanning && state.scanResult.isEmpty(),
    )
}

data class PickerCommonViewStateImpl(
    override val inProgress: Boolean,
    override val msg: String?,
) : PickerCommonViewState

class PickerCreationViewStateImpl(
    override val targetName: String?,
    override val isEnabled: Boolean,
    override val isCreateButtonVisible: Boolean,
    override val isDeleteButtonVisible: Boolean,
) : PickerCreationViewState

class PickerScanningViewStateImpl(
    override val isEnabled: Boolean,
    override val isStartScanButtonVisible: Boolean,
    override val isCancelScanButtonVisible: Boolean,
    override val discoveredItems: Array<PickerItemViewState>,
    override val isEmptyMessageVisible: Boolean,
) : PickerScanningViewState

class PickerItemViewStateImpl(
    override val id: Int,
    override val descriptor: RemoteRoomDescriptor,
) : PickerItemViewState {
    override val shortName = descriptor.title
    override val longName = "${descriptor.title} ${descriptor.description}"
    override val isBold = descriptor.isFavorite
}
