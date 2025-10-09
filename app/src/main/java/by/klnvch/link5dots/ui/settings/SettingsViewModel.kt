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

package by.klnvch.link5dots.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.domain.models.NightMode
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.usecases.GetSettingsUseCase
import by.klnvch.link5dots.domain.usecases.ResetAllDataUseCase
import by.klnvch.link5dots.domain.usecases.SetUserNameUseCase
import by.klnvch.link5dots.domain.usecases.SyncLanguageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val settings: Settings,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveUserNameUseCase: SetUserNameUseCase,
    private val resetAllDataUseCase: ResetAllDataUseCase,
    private val syncLanguageUseCase: SyncLanguageUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsViewState>(SettingsViewState.Loading)
    val uiState: StateFlow<SettingsViewState> = _uiState

    init {
        viewModelScope.launch { syncLanguageUseCase.sync() }
        viewModelScope.launch {
            getSettingsUseCase.get().collect {
                _uiState.value = SettingsViewState.Ready(it)
            }
        }
    }

    val nightMode = settings.nightMode
    fun setUserName(userName: String?) = viewModelScope.launch { saveUserNameUseCase.set(userName) }
    fun setLanguage(language: String) = viewModelScope.launch { settings.setLanguage(language) }
    fun setVibration(isOn: Boolean) = viewModelScope.launch { settings.setVibration(isOn) }
    fun setNightMode(mode: NightMode) = viewModelScope.launch { settings.setNightMode(mode) }
    fun setDotsStyle(style: DotsStyleType) = viewModelScope.launch { settings.setDotsStyle(style) }
    fun reset() = viewModelScope.launch { resetAllDataUseCase.reset() }
}
