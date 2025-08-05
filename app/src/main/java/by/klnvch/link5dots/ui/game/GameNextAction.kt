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

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.ActionAvailability

@Preview
@Composable
fun GameNextActionPreview() {
    GameNextAction(
        menuViewState = MenuViewStateImpl(
            ActionAvailability.Available,
            ActionAvailability.Available,
            ActionAvailability.Available,
        ),
        onNew = {},
        onUndo = {},
        onShare = {},
    )
}

@Composable
fun GameNextAction(
    modifier: Modifier = Modifier,
    menuViewState: MenuViewState,
    onNew: () -> Unit,
    onUndo: () -> Unit,
    onShare: () -> Unit,
) {
    Card(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .alpha(.9f)
    ) {
        if (menuViewState.new.isEnabled) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNew,
            ) {
                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                Text(text = stringResource(R.string.new_game), fontWeight = FontWeight.Bold)
            }
        }
        if (menuViewState.undo.isEnabled) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onUndo,
            ) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                Text(text = stringResource(R.string.undo), fontWeight = FontWeight.Bold)
            }
        }
        if (menuViewState.share.isEnabled) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onShare,
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                Text(text = stringResource(R.string.share), fontWeight = FontWeight.Bold)
            }
        }
    }
}
