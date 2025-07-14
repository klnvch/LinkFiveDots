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
data class PickerViewState(val state: PickerState = createInitialPickerState()) {
    private val isTargetChanging = state.isCreating || state.isDeleting || state.isConnecting

    val common = PickerCommonViewState(
        isTargetChanging || state.isScanning,
        state.error?.message
    )

    val creation = PickerCreationViewState(
        state.result?.let { "${it.title} ${it.description}" },
        !isTargetChanging && !state.isScanning && !state.isConnected,
        !state.isCreated,
        state.isCreated,
    )

    val scanning = PickerScanningViewState(
        !isTargetChanging && !state.isCreated && !state.isConnected,
        !state.isScanning,
        state.isScanning,
        state.scanResult.mapIndexed { id, d -> PickerItemViewState(id, d) }.toTypedArray(),
        state.isScanning && state.scanResult.isEmpty(),
    )
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
data class PickerCommonViewState(
    val inProgress: Boolean,
    val msg: String?,
)

@OptIn(ExperimentalJsExport::class)
@JsExport()
class PickerCreationViewState(
    val targetName: String?,
    val isEnabled: Boolean,
    val isCreateButtonVisible: Boolean,
    val isDeleteButtonVisible: Boolean,
)

@OptIn(ExperimentalJsExport::class)
@JsExport()
class PickerScanningViewState(
    val isEnabled: Boolean,
    val isStartScanButtonVisible: Boolean,
    val isCancelScanButtonVisible: Boolean,
    val discoveredItems: Array<PickerItemViewState>,
    val isEmptyMessageVisible: Boolean,
)

@OptIn(ExperimentalJsExport::class)
@JsExport()
class PickerItemViewState(
    val id: Int,
    val descriptor: RemoteRoomDescriptor,
) {
    val shortName = descriptor.title
    val longName = "${descriptor.title} ${descriptor.description}"
    val isBold = descriptor.isFavorite
}
