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

package by.klnvch.link5dots.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.usecases.GetLanguageChangesUseCase
import by.klnvch.link5dots.domain.usecases.ResetAllDataUseCase
import by.klnvch.link5dots.domain.usecases.SyncNightModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val resetAllDataUseCase: ResetAllDataUseCase,
    private val syncNightModeUseCase: SyncNightModeUseCase,
    private val getLanguageChangesUseCase: GetLanguageChangesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsViewState.READY)
    val uiState: StateFlow<SettingsViewState> = _uiState

    init {
        viewModelScope.launch {
            syncNightModeUseCase.sync()
        }

        viewModelScope.launch {
            getLanguageChangesUseCase.isChanged()
                .collect {
                    if (it) {
                        _uiState.value = SettingsViewState.RESTARTING
                    }
                }
        }
    }

    fun reset() {
        _uiState.value = SettingsViewState.READY
    }

    fun onDeleteAll() {
        viewModelScope.launch {
            _uiState.value = SettingsViewState.WORKING
            resetAllDataUseCase.reset()
            _uiState.value = SettingsViewState.RESTARTING
        }
    }
}
