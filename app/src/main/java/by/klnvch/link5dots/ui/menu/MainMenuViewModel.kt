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

package by.klnvch.link5dots.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.klnvch.link5dots.domain.usecases.CheckTheFirstRunUseCase
import by.klnvch.link5dots.domain.usecases.GetUserNameChangesUseCase
import by.klnvch.link5dots.domain.usecases.SetUserNameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuViewModel @Inject constructor(
    private val checkTheFirstRunUseCase: CheckTheFirstRunUseCase,
    private val getUserNameChangesUseCase: GetUserNameChangesUseCase,
    private val setUserNameUseCase: SetUserNameUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainMenuViewState.initial())
    val uiState: StateFlow<MainMenuViewState> = _uiState

    init {
        viewModelScope.launch {
            checkTheFirstRunUseCase.check()
        }

        viewModelScope.launch {
            getUserNameChangesUseCase.get().collect { _uiState.value = MainMenuViewState(it) }
        }
    }

    fun setUserName(userName: String) {
        viewModelScope.launch {
            setUserNameUseCase.set(userName.trim())
        }
    }
}
