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
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.R

@Composable
fun ListPreferenceItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    @ArrayRes values: Int,
    @ArrayRes labels: Int,
    value: String,
    onChange: (value: String) -> Unit,
) {
    val options = optionsFromResource(values, labels)
    val openDialog = remember { mutableStateOf(false) }
    when {
        openDialog.value -> {
            RadioButtonsDialog(
                icon = icon,
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
        icon = icon,
        title = title,
        value = options[value] ?: "",
        onClick = { openDialog.value = true },
    )
}

@Composable
fun RadioButtonsDialog(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    options: Map<String, String>,
    key: String,
    onConfirmation: (value: String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val (selectedKey, onKeySelected) = remember { mutableStateOf(key) }
    AlertDialog(
        icon = {
            Icon(painter = painterResource(icon), contentDescription = null)
        },
        title = { Text(text = stringResource(title)) },
        text = {
            RadioButtonSingleSelection(
                options = options,
                selectedKey = selectedKey,
                onKeySelected = onKeySelected
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation(selectedKey) }) {
                Text(text = stringResource(R.string.okay))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun RadioButtonSingleSelection(
    modifier: Modifier = Modifier,
    options: Map<String, String>,
    selectedKey: String,
    onKeySelected: (key: String) -> Unit,
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
                Text(
                    text = option.value,
                    style = MaterialTheme.typography.bodyLarge,
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