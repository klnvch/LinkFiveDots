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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.domain.models.DotsStyleType
import by.klnvch.link5dots.ui.common.Circle
import by.klnvch.link5dots.ui.common.Cross
import by.klnvch.link5dots.ui.common.Dot
import kotlinx.coroutines.delay

@Preview()
@Composable
fun GameInfoPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        GameInfo(
            modifier = Modifier.align(Alignment.TopCenter), infoViewState = GameInfoViewStateImpl(
                DotsStyleType.CROSS_AND_RING,
                GameInfoUserViewStateImpl("User 1", 200, true),
                GameInfoUserViewStateImpl("", 0),
            )
        )
    }
}

@Composable
fun GameInfo(modifier: Modifier = Modifier, infoViewState: GameInfoViewState) {
    val user1Dot = when (infoViewState.dotsStyleType) {
        DotsStyleType.ORIGINAL -> Dot
        DotsStyleType.CROSS_AND_RING -> Cross
    }
    val user2Dot = when (infoViewState.dotsStyleType) {
        DotsStyleType.ORIGINAL -> Dot
        DotsStyleType.CROSS_AND_RING -> Circle
    }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Card {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = infoViewState.size,
            )
        }
        Card(
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.End,
                ) {
                    TextDuration(infoViewState.user1)
                    TextDuration(infoViewState.user2)
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    UserDot(infoViewState.user1, user1Dot, Color.Red)
                    UserDot(infoViewState.user2, user2Dot, Color.Blue)
                }
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround,
                ) {
                    TextUserName(infoViewState.user1)
                    TextUserName(infoViewState.user2)
                }
            }
        }
    }
}

@Composable
private fun TextDuration(state: GameInfoUserViewState) {
    var elapsedTime by remember { mutableIntStateOf(0) }
    LaunchedEffect(state) {
        val startTime = state.time
        if (startTime == null) {
            elapsedTime = 0
        } else {
            while (true) {
                val now = (System.currentTimeMillis() / 1000).toInt()
                elapsedTime = now - startTime
                delay(1000)
            }
        }
    }
    Text(
        modifier = Modifier.padding(horizontal = 4.dp),
        text = (state.duration + elapsedTime).formatDuration(),
        fontWeight = if (state.canMove || state.isWon) FontWeight.Bold else null,
        maxLines = 1,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun UserDot(state: GameInfoUserViewState, imageVector: ImageVector, tint: Color) {
    if (state.isWon) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = tint,
        )
    } else {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
private fun TextUserName(state: GameInfoUserViewState) {
    Text(
        modifier = Modifier.padding(horizontal = 4.dp),
        text = state.name,
        fontWeight = if (state.canMove || state.isWon) FontWeight.Bold else null,
        maxLines = 1,
        textAlign = TextAlign.Center,
    )
}
