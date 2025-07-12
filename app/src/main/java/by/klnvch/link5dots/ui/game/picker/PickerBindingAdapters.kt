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
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.game.picker.adapters.PickerAdapter
import by.klnvch.link5dots.ui.game.picker.adapters.PickerItemViewState
import by.klnvch.link5dots.ui.game.picker.states.InvisibleViewState
import by.klnvch.link5dots.ui.game.picker.states.VisibilityViewState
import by.klnvch.link5dots.ui.game.picker.states.VisibleViewState

object PickerBindingAdapters {
    @JvmStatic
    @BindingAdapter("items")
    fun RecyclerView.setItems(items: List<PickerItemViewState>) {
        val currentAdapter = adapter
        if (currentAdapter is PickerAdapter) {
            currentAdapter.submitList(items)
        }
    }

    @JvmStatic
    @BindingAdapter("visibilityDuration")
    fun TextView.setVisibilityDuration(state: VisibilityViewState?) {
        when (state) {
            is InvisibleViewState -> setText(R.string.bluetooth_only_visible_to_paired_devices)
            is VisibleViewState -> text =
                context.getString(R.string.bluetooth_is_discoverable, state.duration)

            else -> visibility = View.GONE
        }
    }
}
