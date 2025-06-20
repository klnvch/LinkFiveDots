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

package by.klnvch.link5dots.ui.scores.scores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.R
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.domain.models.GameResult
import by.klnvch.link5dots.ui.scores.ScoresViewModel

@Composable
fun ScoresTab(
    getVmFactory: () -> SavedStateViewModelFactory,
    viewModel: ScoresViewModel = viewModel(factory = getVmFactory()),
) {
    val uiState by viewModel.scoresUiState.collectAsState()
    val scores = uiState.items
    if (scores.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = scores,
                key = { it.position },
            ) { score ->
                ScoreRow(
                    modifier = Modifier
                        .animateItem()
                        .fillParentMaxWidth(),
                    score = score,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.search_no_results),
            )
        }
    }
}

@Composable
fun ScoreRow(modifier: Modifier, score: HighScoreViewState? = null) {
    Row(
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            modifier = Modifier.weight(1.0f),
            text = score?.position ?: stringResource(R.string.scores_number)
        )
        Text(
            modifier = Modifier.weight(4.0f),
            text = score?.userName ?: stringResource(R.string.name)
        )
        Text(
            modifier = Modifier.weight(2.0f),
            text = score?.size ?: stringResource(R.string.scores_moves)
        )
        Text(
            modifier = Modifier.weight(3.0f),
            text = score?.duration ?: stringResource(R.string.time)
        )
        Text(
            modifier = Modifier.weight(3.0f),
            text = when (score?.status) {
                GameResult.WON -> stringResource(R.string.scores_won)
                GameResult.LOST -> stringResource(R.string.scores_lost)
                else -> stringResource(R.string.status)
            }
        )
    }
}
