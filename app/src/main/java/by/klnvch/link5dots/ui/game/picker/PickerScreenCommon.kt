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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.FoundRemoteRoom
import by.klnvch.link5dots.ui.common.CustomButtonWithText


@Composable
fun PickerScreenCommon(
    uiState: PickerViewState,
    onCreate: () -> Unit,
    onDelete: () -> Unit,
    onScan: () -> Unit,
    onCancel: () -> Unit,
    onConnect: (invitation: FoundRemoteRoom) -> Unit,
) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> PickerScreenPortrait(
            uiState,
            onCreate,
            onDelete,
            onScan,
            onCancel,
            onConnect,
        )

        else -> PickerScreenLandscape(
            uiState,
            onCreate,
            onDelete,
            onScan,
            onCancel,
            onConnect,
        )
    }
}

@Composable
private fun PickerScreenPortrait(
    uiState: PickerViewState,
    onCreate: () -> Unit,
    onDelete: () -> Unit,
    onScan: () -> Unit,
    onCancel: () -> Unit,
    onConnect: (invitation: FoundRemoteRoom) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CommonPart(uiState.common)
        CreationPart(uiState.creation, onCreate, onDelete)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        ScanPart(uiState.scanning, onScan, onCancel, onConnect)
    }
}

@Composable
private fun PickerScreenLandscape(
    uiState: PickerViewState,
    onCreate: () -> Unit,
    onDelete: () -> Unit,
    onScan: () -> Unit,
    onCancel: () -> Unit,
    onConnect: (invitation: FoundRemoteRoom) -> Unit,
) {
    Column {
        CommonPart(uiState.common)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CreationPart(uiState.creation, onCreate, onDelete)
            VerticalDivider()
            ScanPart(uiState.scanning, onScan, onCancel, onConnect)
        }
    }
}

@Composable
private fun CommonPart(uiState: PickerCommonViewState) {
    val progressAlpha = if (uiState.inProgress) 1f else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .alpha(progressAlpha)
                .height(16.dp)
                .fillMaxWidth()
        )
        uiState.msg?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CreationPart(
    uiState: PickerCreationViewState,
    onCreate: () -> Unit,
    onDelete: () -> Unit,
) {
    if (uiState.isCreateButtonVisible) {
        CustomButtonWithText(
            textId = R.string.create,
            onClick = onCreate,
            enabled = uiState.isEnabled,
        )
    }
    if (uiState.isDeleteButtonVisible) {
        CustomButtonWithText(
            textId = R.string.delete,
            onClick = onDelete,
            enabled = uiState.isEnabled,
        )
    }

    val statusAlpha = if (uiState.isEnabled) 1f else .2f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .alpha(statusAlpha)
    ) {
        Text(
            text = stringResource(R.string.name) + ":"
        )
        if (uiState.text.isEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(R.string.name_not_set)
            )
        } else {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                uiState.text.map {
                    when (it) {
                        is String -> Text(text = it)
                        is PassedTime -> PassedTimeText(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanPart(
    uiState: PickerScanningViewState,
    onScan: () -> Unit,
    onCancel: () -> Unit,
    onConnect: (invitation: FoundRemoteRoom) -> Unit,
) {
    if (uiState.isStartScanButtonVisible) {
        CustomButtonWithText(
            textId = R.string.scan,
            onClick = onScan,
            enabled = uiState.isEnabled,
        )
    }
    if (uiState.isCancelScanButtonVisible) {
        CustomButtonWithText(
            textId = R.string.cancel,
            onClick = onCancel,
            enabled = uiState.isEnabled,
        )
    }

    val openConnectDialog = remember { mutableStateOf<PickerItemViewState?>(null) }
    openConnectDialog.value?.let {
        AlertDialog(
            text = {
                Text(
                    text = stringResource(R.string.connection_dialog_text, it.shortName)
                )
            },
            onDismissRequest = { openConnectDialog.value = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        openConnectDialog.value = null
                        onConnect(it.descriptor)
                    }
                ) {
                    Text(text = stringResource(R.string.okay))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openConnectDialog.value = null }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.widthIn(0.dp, 320.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = uiState.discoveredItems,
            key = { it.id },
        ) { room ->
            Card(
                modifier = Modifier
                    .animateItem()
                    .fillParentMaxWidth(),
                onClick = { openConnectDialog.value = room }
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    room.longName.map { Text(text = it) }
                }
            }
        }
    }

    if (uiState.isEmptyMessageVisible) {
        Text(text = stringResource(R.string.search_no_results))
    }
}

@Composable
fun PassedTimeText(passedTime: PassedTime) {
    when (passedTime.unit) {
        PassedTimeUnit.JustNow -> Text(stringResource(R.string.posted_just_now))
        PassedTimeUnit.Minutes -> Text(
            pluralStringResource(
                R.plurals.num_minutes_ago,
                passedTime.count,
                passedTime.count,
            )
        )

        PassedTimeUnit.Hours -> Text(
            pluralStringResource(
                R.plurals.num_hours_ago,
                passedTime.count,
                passedTime.count,
            )
        )

        PassedTimeUnit.Days -> Text(
            pluralStringResource(
                R.plurals.num_days_ago,
                passedTime.count,
                passedTime.count,
            )
        )
    }
}