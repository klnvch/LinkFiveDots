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

package by.klnvch.link5dots.ui.game.topBar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.IconMenuItem
import by.klnvch.link5dots.ui.common.NavigationIcon
import by.klnvch.link5dots.ui.common.topAppBarColors
import by.klnvch.link5dots.ui.game.MenuViewState
import by.klnvch.link5dots.ui.game.OfflineGameViewModel

@Composable
fun TopBar(
    viewModel: OfflineGameViewModel,
    title: @Composable () -> Unit,
    navigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    GameTopBar(
        title = title,
        navigateUp = navigateUp,
        viewState = uiState.menuViewState,
        onNew = { viewModel.newGame() },
        onUndo = { viewModel.undoLastMove() },
    )
}

@Composable
private fun GameActions(
    viewState: MenuViewState,
    onNew: () -> Unit,
    onUndo: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    if (viewState.undoOption.isVisible || viewState.newOption.isVisible) {
        IconMenuItem(
            imageVector = Icons.Filled.MoreVert,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GameDropdownMenuItem(viewState.undoOption, R.string.undo) {
                onUndo()
            }
            GameDropdownMenuItem(viewState.newOption, R.string.new_game) {
                onNew()
                expanded = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTopBar(
    viewState: MenuViewState,
    title: @Composable () -> Unit,
    navigateUp: () -> Unit,
    onNew: () -> Unit,
    onUndo: () -> Unit,
) {
    TopAppBar(
        title = title,
        colors = topAppBarColors(),
        navigationIcon = { NavigationIcon(onClick = navigateUp) },
        actions = { GameActions(viewState, onNew, onUndo) },
    )
}
