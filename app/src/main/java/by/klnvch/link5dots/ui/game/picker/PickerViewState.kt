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
import by.klnvch.link5dots.ui.game.picker.adapters.PickerItemViewState
import by.klnvch.link5dots.ui.game.picker.states.PickerState

data class PickerViewState(private val state: PickerState) {
    val targetViewState = state.result?.let { TargetViewState(it) }

    val isCreateOrDeleteButtonEnabled = !state.inProgress || !state.isScanning || !state.isConnected

    private val isCreateVisible = !state.isCreated
    val createButtonVisibility = if (isCreateVisible) View.VISIBLE else View.GONE
    val deleteButtonVisibility = if (isCreateVisible) View.GONE else View.VISIBLE

    val progressCreateVisibility =
        if (state.isCreating || state.isDeleting) View.VISIBLE else View.INVISIBLE

    val statusVisibility =
        if (state.isCreating || state.isDeleting) View.INVISIBLE else View.VISIBLE

    val isScanButtonEnabled = !state.isConnected

    val scanWarningMessage = when {
        state.isScanning && state.scanResult.isEmpty() -> null
        else -> ""
    }

    private val isScanVisible = !state.isScanning
    val startScanButtonVisibility = if (isScanVisible) View.VISIBLE else View.GONE
    val cancelScanButtonVisibility = if (isScanVisible) View.GONE else View.VISIBLE
    val scanProgressVisibility = if (state.isScanning) View.VISIBLE else View.INVISIBLE
    val discoveredItems = state.scanResult.map { PickerItemViewState(it) }
}
