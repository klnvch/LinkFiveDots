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

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.ui.game.picker.states.BluetoothPickerViewState
import by.klnvch.link5dots.ui.game.picker.states.InvisibleViewState
import by.klnvch.link5dots.ui.game.picker.states.VisibleViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class VisibilityViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BluetoothPickerViewState(InvisibleViewState))
    val uiState: StateFlow<BluetoothPickerViewState> = _uiState.asStateFlow()
    private var _timer: CountDownTimer? = null

    fun startCountDown(durationSeconds: Int) {
        _timer?.cancel()

        val duration = durationSeconds * 1000L
        _uiState.value = _uiState.value.copy(visibility = VisibleViewState(duration))

        _timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value =
                    _uiState.value.copy(visibility = VisibleViewState(millisUntilFinished))
            }

            override fun onFinish() {
                _uiState.value = _uiState.value.copy(visibility = InvisibleViewState)
            }
        }.start()
    }

    override fun onCleared() {
        _timer?.cancel()
        super.onCleared()
    }

    companion object {
        const val KEY = "VISIBILITY_VIEW_MODEL_KEY"
    }
}
