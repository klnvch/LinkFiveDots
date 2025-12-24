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

package by.klnvch.link5dots.ui.game.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.usecases.OnlineGameShortInfo
import by.klnvch.link5dots.domain.usecases.OnlineGameShortInfoImpl
import by.klnvch.link5dots.domain.usecases.OnlineGameShortInfoStatus
import by.klnvch.link5dots.ui.common.BlueDot
import by.klnvch.link5dots.ui.common.RedDot
import by.klnvch.link5dots.ui.common.TextCenterInfo
import by.klnvch.link5dots.ui.game.viewmodels.OnlineUserHistoryViewModel
import by.klnvch.link5dots.ui.game.viewmodels.UserHistoryViewState

@Composable
fun HistoryRow(
    item: OnlineGameShortInfo,
    modifier: Modifier = Modifier,
) {
    // ---- Helper to decide icon/color based on status -----------------
    val (statusIcon, statusColor, statusText) = when (item.status) {
        OnlineGameShortInfoStatus.Won -> Triple(
            Icons.Default.EmojiEvents,
            MaterialTheme.colorScheme.primary,
            "Won"
        )

        OnlineGameShortInfoStatus.Lost -> Triple(
            Icons.Default.Close,
            MaterialTheme.colorScheme.error,
            "Lost"
        )

        OnlineGameShortInfoStatus.Draw -> Triple(
            Icons.Default.Remove,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Draw"
        )

        OnlineGameShortInfoStatus.InProgress -> Triple(
            Icons.Default.Timer,
            MaterialTheme.colorScheme.secondary,
            "In‑Progress"
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Status icon + text (left side)
            Icon(
                imageVector = statusIcon,
                contentDescription = statusText,
                tint = statusColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Player names with coloured dots
            Column(
                modifier = Modifier.weight(1f)          // take up remaining space
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RedDot()
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.user1Name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BlueDot()
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.user2Name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = "${stringResource(R.string.settings_dots)}: ${item.sizeText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${stringResource(R.string.time)}: ${item.timeText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Game timestamp (right side)
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${stringResource(R.string.duration)}: ${item.durationText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryItemRowPreview() {
    val sample = OnlineGameShortInfoImpl(
        user1Name = "Alice",
        user2Name = "Bob",
        timeText = "Dec 18, 05:22 PM",
        durationText = "12:34",
        sizeText = "12",
        status = OnlineGameShortInfoStatus.Won,
    )
    MaterialTheme {
        Surface {
            Column {
                HistoryRow(item = sample, modifier = Modifier.clickable {})
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun OnlineUserHistory(
    viewModel: OnlineUserHistoryViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState
    LaunchedEffect(true) {
        viewModel.load()
    }
    when (state) {
        is UserHistoryViewState.Loading -> {
            TextCenterInfo(R.string.loading)
        }

        is UserHistoryViewState.Empty -> {
            TextCenterInfo(R.string.search_no_results)
        }

        is UserHistoryViewState.Ready -> {
            LazyColumn {
                items(state.items) { HistoryRow(it) }
            }
        }
    }
}
