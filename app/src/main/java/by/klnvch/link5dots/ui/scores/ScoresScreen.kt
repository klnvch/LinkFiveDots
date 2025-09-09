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

package by.klnvch.link5dots.ui.scores

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.R
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.ui.menu.Screen
import by.klnvch.link5dots.ui.scores.history.HistoryTab
import by.klnvch.link5dots.ui.scores.scores.ScoresTab

enum class ScoresDestination(
    val title: Int = 0,
) {
    SCORES(R.string.scores_title),
    HISTORY(R.string.history),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    getSSVMFactory: () -> SavedStateViewModelFactory,
    viewModel: ScoresViewModel = viewModel(factory = getSSVMFactory()),
    onNavigate: (Screen) -> Unit,
    onSnackbarMessage: (message: String, actionLabel: String, action: () -> Unit) -> Unit,
) {
    var selectedDestination by rememberSaveable { mutableIntStateOf(viewModel.getCurrentItem()) }

    Column {
        PrimaryTabRow(
            selectedTabIndex = selectedDestination,
        ) {
            ScoresDestination.entries.forEachIndexed { index, destination ->
                Tab(
                    selected = selectedDestination == index,
                    onClick = {
                        selectedDestination = index
                        viewModel.setCurrentItem(index)
                    },
                    text = {
                        Text(
                            stringResource(destination.title),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        when (selectedDestination) {
            ScoresDestination.SCORES.ordinal -> ScoresTab(getSSVMFactory)
            ScoresDestination.HISTORY.ordinal -> HistoryTab(
                onNavigate,
                onSnackbarMessage,
                getSSVMFactory
            )
        }
    }
}
