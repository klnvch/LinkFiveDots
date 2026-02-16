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
import by.klnvch.link5dots.domain.history.entities.OnlineGameShortInfo
import by.klnvch.link5dots.domain.history.entities.OnlineGameShortInfoStatus
import by.klnvch.link5dots.domain.history.entities.Opponent
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

            // Player names with colored dots
            Column(
                modifier = Modifier.weight(1f)          // take up remaining space
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.opponent.name,
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
    val sample = object : OnlineGameShortInfo {
        override val opponent = object : Opponent {
            override val id = "id"
            override val name = "Alice"
        }
        override val timeText = "Dec 18, 05:22 PM"
        override val durationText = "12:34"
        override val sizeText = "12"
        override val status = OnlineGameShortInfoStatus.Won
    }
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
