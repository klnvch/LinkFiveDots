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

package by.klnvch.link5dots.ui.scores.history

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.RoomType
import by.klnvch.link5dots.ui.common.BlueDot
import by.klnvch.link5dots.ui.common.RedDot
import by.klnvch.link5dots.ui.common.TextCenterInfo
import by.klnvch.link5dots.ui.menu.Screen

@Composable
fun HistoryTab(
    onNavigate: (Screen) -> Unit,
    onSnackbarMessage: (message: String, actionLabel: String, action: () -> Unit) -> Unit,
    getVMFactory: () -> ViewModelProvider.Factory,
    historyViewModel: HistoryViewModel = viewModel(factory = getVMFactory()),
) {
    val uiState by historyViewModel.historyUiState.collectAsState()
    val state = uiState
    val types = remember { mutableStateListOf(*RoomType.entries.toTypedArray()) }
    val snackBarMessage = stringResource(R.string.done)
    val snackBarActionLabel = stringResource(R.string.undo)

    LaunchedEffect(types.size) {
        historyViewModel.load(types)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row {
            TypeDropdownMenu(types)
        }
        when (state) {
            is HistoryViewStateLoading -> {
                TextCenterInfo(R.string.loading)
            }

            is HistoryViewStateCompleted -> {
                val rooms = state.items
                if (rooms.isNotEmpty()) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = rooms,
                            key = { it.room.key },
                        ) { room ->
                            HistoryRoomRow(
                                modifier = Modifier
                                    .animateItem()
                                    .fillParentMaxWidth(),
                                room = room,
                                onClick = { onNavigate(Screen.GameInfo(it)) },
                                onRemove = {
                                    historyViewModel.deleteRoom(it.room)
                                    onSnackbarMessage(
                                        snackBarMessage,
                                        snackBarActionLabel
                                    ) { historyViewModel.insertRoom(it.room) }
                                },
                            )
                        }
                    }
                } else {
                    TextCenterInfo(R.string.search_no_results)
                }
            }
        }
    }
}

@Composable
fun HistoryRoomRow(
    modifier: Modifier,
    room: HistoryItemViewState,
    onClick: (key: String) -> Unit,
    onRemove: (HistoryItemViewState) -> Unit,
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = modifier.fillMaxSize(),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red)
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = Color.White
                    )
                }

                StartToEnd -> {}
                Settled -> {}
            }
        },
        onDismiss = { onRemove(room) },
    ) {
        Card(
            onClick = { onClick(room.room.key) },
            modifier = modifier,
        ) {
            Row {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1.0f),
                ) {
                    UserName({ RedDot() }, room.userName1)
                    RoomProperty(R.string.type, stringResource(room.typeStringRes))
                    RoomProperty(R.string.time, room.startTime)
                    RoomProperty(R.string.duration, room.duration)
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    UserName({ BlueDot() }, room.userName2)
                    RoomProperty(R.string.settings_dots, room.size)
                }
            }
        }
    }
}

@Composable
private fun UserName(dot: @Composable () -> Unit, userName: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        dot()
        Text(
            text = userName ?: "",
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RoomProperty(@StringRes textId: Int, value: String) {
    Row {
        Text(
            text = stringResource(textId) + stringResource(R.string.colon),
        )
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = value,
            fontWeight = FontWeight.Bold,
        )
    }
}
