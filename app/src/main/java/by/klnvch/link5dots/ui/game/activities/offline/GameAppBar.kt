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

package by.klnvch.link5dots.ui.game.activities.offline

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.game.MenuViewState

@Composable
fun GameAppBarTitle(@StringRes textId: Int) {
    Text(
        stringResource(textId),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAppBar(
    viewState: MenuViewState,
    title: @Composable () -> Unit,
    navigateUp: () -> Unit,
    onNew: () -> Unit,
    onUndo: () -> Unit,
    onFocus: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(33, 33, 33),
            titleContentColor = Color(250, 250, 250),
            navigationIconContentColor = Color(250, 250, 250),
        ),
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = ""
                )
            }
        },
        actions = {
            IconButton(onClick = onFocus) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = Color(250, 250, 250),
                )
            }
            if (viewState.undo.isVisible || viewState.new.isVisible) {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = null,
                        tint = Color(250, 250, 250),
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (viewState.undo.isVisible) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.undo)) },
                            onClick = onUndo,
                            enabled = viewState.undo.isEnabled,
                        )
                    }
                    if (viewState.undo.isVisible) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.new_game)) },
                            onClick = {
                                onNew
                                expanded = false
                            },
                            enabled = viewState.new.isEnabled,
                        )
                    }
                }
            }
        },
    )
}
