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

package by.klnvch.link5dots.ui.settings.items

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.ui.common.DialogCancelButton
import by.klnvch.link5dots.ui.common.DialogConfirmButton
import by.klnvch.link5dots.ui.common.adaptive.AdaptiveIcon
import by.klnvch.link5dots.ui.common.adaptive.AdaptiveText

@Composable
fun <Key> ListPreferenceItem(
    imageVector: ImageVector,
    @StringRes title: Int,
    options: Map<Key, String>,
    value: Key,
    onChange: (value: Key) -> Unit,
) {
    val openDialog = remember { mutableStateOf(false) }
    when {
        openDialog.value -> {
            RadioButtonsDialog(
                imageVector = imageVector,
                title = title,
                options = options,
                key = value,
                onConfirmation = {
                    openDialog.value = false
                    onChange(it)
                },
                onDismissRequest = { openDialog.value = false })
        }
    }

    PreferenceItem(
        imageVector = imageVector,
        title = title,
        value = options[value] ?: "",
        onClick = { openDialog.value = true },
    )
}

@Composable
fun <Key> RadioButtonsDialog(
    imageVector: ImageVector,
    @StringRes title: Int,
    options: Map<Key, String>,
    key: Key,
    onConfirmation: (value: Key) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val (selectedKey, onKeySelected) = remember { mutableStateOf(key) }
    AlertDialog(
        icon = { AdaptiveIcon(imageVector) },
        title = { AdaptiveText(text = stringResource(title)) },
        text = {
            RadioButtonSingleSelection(
                options = options,
                selectedKey = selectedKey,
                onKeySelected = onKeySelected
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = { DialogConfirmButton(onClick = { onConfirmation(selectedKey) }) },
        dismissButton = { DialogCancelButton(onClick = { onDismissRequest() }) }
    )
}

@Composable
fun <Key> RadioButtonSingleSelection(
    modifier: Modifier = Modifier,
    options: Map<Key, String>,
    selectedKey: Key,
    onKeySelected: (key: Key) -> Unit,
) {
    Column(
        modifier
            .selectableGroup()
            .verticalScroll(rememberScrollState())
    ) {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option.key == selectedKey),
                        onClick = { onKeySelected(option.key) },
                        role = Role.RadioButton
                    )
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option.key == selectedKey),
                    onClick = null
                )
                AdaptiveText(
                    text = option.value,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
@ReadOnlyComposable
fun optionsFromResource(
    @ArrayRes entryValues: Int,
    @ArrayRes entries: Int,
): Map<String, String> {
    val values = stringArrayResource(entryValues)
    val labels = stringArrayResource(entries)
    val options = mutableMapOf<String, String>()
    for (i in values.indices) {
        options.put(values[i], labels[i])
    }
    return options
}