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

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.PermissionException
import by.klnvch.link5dots.ui.common.CustomButtonWithText
import by.klnvch.link5dots.ui.common.TextNoSurface
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.picker.states.InvisibleViewState
import by.klnvch.link5dots.ui.game.picker.states.VisibilityViewState
import by.klnvch.link5dots.ui.game.picker.states.VisibleViewState

@Composable
fun BluetoothPickerScreen(
    gameViewModel: OnlineGameViewModel,
    bluetoothViewModel: VisibilityViewModel,
) {
    val context = LocalContext.current
    val uiState by gameViewModel.pickerUiState.collectAsState()
    val uiVisibilityState by bluetoothViewModel.uiState.collectAsState()
    val setPermissionError = { gameViewModel.setError(PermissionException()) }

    val requestCreatePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) gameViewModel.createRoom() else setPermissionError()
    }

    val requestScanPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val isGranted = result.values.all { it }
        gameViewModel.startScan(if (isGranted) null else PermissionException())
    }

    val startDiscoverableForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_CANCELED) {
                bluetoothViewModel.startCountDown(it.resultCode)
            }
        }

    val requestDiscoverPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) startDiscoverableForResult.launch(discoverableIntent)
            else setPermissionError()
        }

    Column {
        BluetoothVisibilityPart(uiVisibilityState.visibility) {
            context.discoveryPermissionGuard(
                requestDiscoverPermissionLauncher,
                startDiscoverableForResult
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        PickerScreenCommon(
            uiState = uiState,
            onCreate = {
                context.createPermissionGuard(
                    launcher = requestCreatePermissionLauncher,
                    onPermissionGranted = { gameViewModel.createRoom() },
                )
            },
            onDelete = { gameViewModel.deleteRoom() },
            onScan = {
                context.scanPermissionGuard(
                    launcher = requestScanPermissionLauncher,
                    onPermissionGranted = { gameViewModel.startScan() }
                )
            },
            onCancel = { gameViewModel.stopScan() },
            onConnect = { gameViewModel.connect(it) },
        )
    }
}

@Composable
fun BluetoothVisibilityPart(state: VisibilityViewState, onSetVisible: () -> Unit) {
    val text = when (state) {
        is InvisibleViewState -> stringResource(R.string.bluetooth_only_visible_to_paired_devices)
        is VisibleViewState -> stringResource(R.string.bluetooth_is_discoverable, state.duration)
    }
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        CustomButtonWithText(
            textId = R.string.bluetooth_not_discoverable,
            onClick = onSetVisible,
        )
        TextNoSurface(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}
