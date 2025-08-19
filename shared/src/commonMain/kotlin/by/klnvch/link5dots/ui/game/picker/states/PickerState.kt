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

package by.klnvch.link5dots.ui.game.picker.states

import by.klnvch.link5dots.domain.models.RemoteRoomDescriptor
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport()
interface PickerState {
    val isNone: Boolean

    val isCreating: Boolean
    val isCreated: Boolean
    val isDeleting: Boolean
    val result: RemoteRoomDescriptor?

    val isScanning: Boolean
    val scanResult: Array<RemoteRoomDescriptor>

    val isConnected: Boolean
    val isConnecting: Boolean
    val isDisconnected: Boolean

    val error: Throwable?

    fun reset(): PickerState
    fun failed(e: Throwable): PickerState

    fun creating(): PickerState
    fun created(d: RemoteRoomDescriptor): PickerState
    fun deleting(): PickerState

    fun scanning(items: Array<RemoteRoomDescriptor> = emptyArray()): PickerState
    fun scanDone(): PickerState

    fun connecting(): PickerState
    fun connected(d: RemoteRoomDescriptor): PickerState
    fun disconnected(): PickerState
}

@OptIn(ExperimentalJsExport::class)
@JsExport()
fun createInitialPickerState(): PickerState = PickerStateImpl()

private data class PickerStateImpl(
    private val targetState: TargetState = TargetNone,
    private val scanState: ScanState = ScanNone,
    private val connectState: ConnectState = ConnectNone,
    override val error: Throwable? = null,
) : PickerState {
    override val isNone =
        targetState is TargetNone && scanState is ScanNone && connectState is ConnectNone

    override val isCreating = targetState is TargetCreating
    override val isCreated = targetState is TargetCreated
    override val isDeleting = targetState is TargetDeleting
    override val result = if (targetState is TargetCreated) targetState.descriptor else null

    override val isScanning = scanState is ScanOn
    override val scanResult =
        if (scanState is ScanWithResult) scanState.items.toTypedArray() else emptyArray()

    override val isConnected = connectState is ConnectConnected
    override val isConnecting = connectState is ConnectConnecting
    override val isDisconnected = connectState is ConnectDisconnected

    override fun reset() = createInitialPickerState()
    override fun failed(e: Throwable) = PickerStateImpl(error = e)

    override fun creating() = copy(targetState = TargetCreating)
    override fun created(d: RemoteRoomDescriptor) = copy(targetState = TargetCreated(d))
    override fun deleting() = copy(targetState = TargetDeleting)

    override fun scanning(items: Array<RemoteRoomDescriptor>) =
        copy(scanState = ScanOn(items.toList()))

    override fun scanDone() =
        if (scanState is ScanOn) copy(scanState = ScanDone(scanState.items)) else this

    override fun connecting() = copy(connectState = ConnectConnecting)
    override fun connected(d: RemoteRoomDescriptor) =
        copy(targetState = TargetNone, scanState = ScanNone, connectState = ConnectConnected(d))

    override fun disconnected() =
        if (connectState is ConnectConnected) copy(connectState = ConnectDisconnected) else this
}
