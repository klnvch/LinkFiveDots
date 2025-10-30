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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.DialogCancelButton
import by.klnvch.link5dots.ui.common.DialogConfirmButton
import by.klnvch.link5dots.ui.common.adaptive.AdaptiveIcon
import by.klnvch.link5dots.ui.common.adaptive.AdaptiveText

@Composable
fun DeleteAllPreferenceItem(onClick: () -> Unit) {
    val openDialog = remember { mutableStateOf(false) }
    PreferenceItem(
        imageVector = Icons.Filled.Delete,
        title = R.string.main_clear_title,
        onClick = { openDialog.value = true },
    )
    when {
        openDialog.value -> {
            AlertDialog(
                icon = { AdaptiveIcon(imageVector = Icons.Filled.Delete) },
                title = { AdaptiveText(text = stringResource(R.string.main_clear_confirm_title)) },
                onDismissRequest = { openDialog.value = false },
                confirmButton = {
                    DialogConfirmButton(onClick = {
                        openDialog.value = false
                        onClick()
                    })
                },
                dismissButton = { DialogCancelButton(onClick = { openDialog.value = false }) }
            )
        }
    }
}
