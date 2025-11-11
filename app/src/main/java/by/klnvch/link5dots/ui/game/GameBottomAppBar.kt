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

package by.klnvch.link5dots.ui.game

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.adaptive.AdaptiveIcon
import by.klnvch.link5dots.ui.common.adaptive.AdaptiveText
import by.klnvch.link5dots.ui.game.viewmodels.GameActions

@Composable
private fun BottomBarButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    imageVector: ImageVector,
    @StringRes textId: Int,
    onClick: () -> Unit,
) {
    if (isVisible) {
        TextButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            AdaptiveIcon(
                imageVector = imageVector,
                tint = Color(250, 250, 250),
            )
            AdaptiveText(
                text = stringResource(textId),
                fontWeight = FontWeight.Bold,
                color = Color(250, 250, 250)
            )
        }
    }
}

@Composable
private fun BottomBar(
    menuViewState: MenuViewState,
    onNew: () -> Unit,
    onUndo: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarButton(
            Modifier.weight(1f),
            menuViewState.newOption.isEnabled,
            Icons.Filled.Refresh,
            R.string.new_game,
            onNew
        )
        BottomBarButton(
            Modifier.weight(1f),
            menuViewState.undoOption.isEnabled,
            Icons.Filled.Clear,
            R.string.undo,
            onUndo
        )
        BottomBarButton(
            Modifier.weight(1f),
            menuViewState.shareOption.isEnabled,
            Icons.Filled.Share,
            R.string.share,
            onShare
        )
    }
}

@Composable
private fun BottomBarDefault(
    menuViewState: MenuViewState,
    onUndo: () -> Unit,
    onFocus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarButton(
            Modifier.weight(1f),
            menuViewState.undoOption.isEnabled,
            Icons.Filled.Clear,
            R.string.undo,
            onUndo
        )
        BottomBarButton(
            Modifier.weight(1f),
            true,
            Icons.Filled.Search,
            R.string.search,
            onFocus
        )
    }
}

@Composable
fun GameBottomAppBar(
    actions: GameActions,
    onNewGameNotImplemented: (() -> Unit)? = null,
) {
    val uiState by actions.uiState.collectAsState()

    BottomAppBar(
        containerColor = Color(33, 33, 33),
        contentColor = Color(250, 250, 250),
    ) {
        if (uiState.isOver) {
            BottomBar(
                uiState.menuViewState,
                { if (!actions.new()) onNewGameNotImplemented?.invoke() },
                { actions.undo() },
                { actions.saveScore() },
            )
        } else {
            BottomBarDefault(
                uiState.menuViewState,
                { actions.undo() },
                { actions.focus() },
            )
        }
    }
}
