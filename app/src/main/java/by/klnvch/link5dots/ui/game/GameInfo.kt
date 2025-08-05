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

import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.DotsStyleType

@Preview()
@Composable
fun GameInfoPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        GameInfo(
            modifier = Modifier.align(Alignment.TopCenter),
            infoViewState = GameInfoViewStateImpl(
                DotsStyleType.CROSS_AND_RING,
                GameInfoUserViewStateImpl("User 1", "00:00", true),
                GameInfoUserViewStateImpl("", ""),
            )
        )
    }
}

@Composable
fun GameInfo(modifier: Modifier = Modifier, infoViewState: GameInfoViewState) {
    val user1Dot = when (infoViewState.dotsStyleType) {
        DotsStyleType.ORIGINAL -> R.drawable.game_dot_circle_red
        DotsStyleType.CROSS_AND_RING -> R.drawable.game_dot_cross_red
    }
    val user2Dot = when (infoViewState.dotsStyleType) {
        DotsStyleType.ORIGINAL -> R.drawable.game_dot_circle_blue
        DotsStyleType.CROSS_AND_RING -> R.drawable.game_dot_ring_blue
    }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Card {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = infoViewState.size,
            )
        }
        Card(
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(8.dp)
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
                ) {
                    UserDot(dotResId = user1Dot)
                    UserDot(dotResId = user2Dot)
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
    Text(
        modifier = Modifier.padding(horizontal = 4.dp),
        text = state.duration,
        fontWeight = if (state.canMove || state.isWon) FontWeight.Bold else null,
        maxLines = 1,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun UserDot(@DrawableRes dotResId: Int) {
    Icon(
        painter = painterResource(id = dotResId),
        contentDescription = null,
        tint = Color.Unspecified,
    )
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
