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

import by.klnvch.link5dots.currentTime
import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.formatDateTime
import by.klnvch.link5dots.ui.game.picker.states.PickerState
import by.klnvch.link5dots.ui.game.picker.states.createInitialPickerState
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.time.Duration.Companion.seconds

enum class PassedTimeUnit { JustNow, Minutes, Hours, Days }
data class PassedTime(val time: Int) {
    init {
        val dt = (currentTime() / 1000).toInt() - time
        dt.seconds.toComponents { days, hours, minutes, seconds, nanoseconds ->
            if (days > 0) {
                unit = PassedTimeUnit.Days
                count = days.toInt()
            } else if (hours > 0) {
                unit = PassedTimeUnit.Hours
                count = hours
            } else if (minutes > 0) {
                unit = PassedTimeUnit.Minutes
                count = minutes
            } else {
                unit = PassedTimeUnit.JustNow
                count = seconds
            }
        }
    }

    val unit: PassedTimeUnit
    val count: Int
}

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
    val text: Array<Any>
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
    val descriptor: FoundRemoteRoom
    val shortName: String
    val longName: Array<String>
    val isBold: Boolean
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
fun PickerState.toPickerViewState(): PickerViewState = PickerViewStateImpl(this)

///////////////////////////////////////////////////////////////////////////////////////////////////
// Implementation
///////////////////////////////////////////////////////////////////////////////////////////////////

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
        listOfNotNull(
            state.result?.title,
            state.result?.description,
            state.result?.time?.formatDateTime()
        ).toTypedArray(),
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
    override val text: Array<Any>,
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
    override val descriptor: FoundRemoteRoom,
) : PickerItemViewState {
    override val shortName = descriptor.title
    override val longName =
        listOfNotNull(
            descriptor.title,
            descriptor.description,
            descriptor.time?.formatDateTime()
        ).toTypedArray()
    override val isBold = descriptor.isFavorite
}
