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

package by.klnvch.link5dots.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.NightMode
import by.klnvch.link5dots.ui.common.TextCenterInfo
import by.klnvch.link5dots.ui.settings.items.DotsStylePreferenceItem
import by.klnvch.link5dots.ui.settings.items.EditTextPreferenceItem
import by.klnvch.link5dots.ui.settings.items.ListPreferenceItem
import by.klnvch.link5dots.ui.settings.items.PreferenceItem
import by.klnvch.link5dots.ui.settings.items.SwitchPreferenceItem
import by.klnvch.link5dots.ui.settings.items.optionsFromResource

@Composable
fun SettingsScreen(
    getVMFactory: () -> ViewModelProvider.Factory,
    viewModel: SettingsViewModel = viewModel(factory = getVMFactory()),
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState

    val languageOptions =
        optionsFromResource(R.array.languages_values, R.array.languages_entries)

    val nightModeOptions = mapOf(
        NightMode.System to stringResource(R.string.settings_system),
        NightMode.On to stringResource(R.string.switch_on_text),
        NightMode.Off to stringResource(R.string.switch_off_text),
    )

    when (state) {
        SettingsViewState.Loading -> TextCenterInfo(R.string.loading)
        is SettingsViewState.Ready -> Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            EditTextPreferenceItem(
                imageVector = Icons.Filled.Mood,
                title = R.string.username,
                value = state.settings.userName ?: stringResource(R.string.unknown),
                onChange = { viewModel.setUserName(it) },
            )
            ListPreferenceItem(
                imageVector = Icons.Filled.Language,
                title = R.string.settings_language,
                options = languageOptions,
                value = state.settings.language,
                onChange = { viewModel.setLanguage(it) },
            )
            SwitchPreferenceItem(
                imageVector = Icons.Filled.Vibration,
                title = R.string.settings_vibration,
                value = state.settings.isVibrationEnabled,
                onChange = { viewModel.setVibration(it) },
            )
            ListPreferenceItem(
                imageVector = Icons.Filled.WbSunny,
                title = R.string.settings_night_mode,
                options = nightModeOptions,
                value = state.settings.nightMode,
                onChange = { viewModel.setNightMode(it) },
            )
            DotsStylePreferenceItem(
                value = state.settings.dotsStyle,
                onChange = { viewModel.setDotsStyle(it) },
            )
            PreferenceItem(
                imageVector = Icons.Filled.Delete,
                title = R.string.settings_delete_all,
                onClick = { viewModel.reset() },
            )
        }
    }
}
