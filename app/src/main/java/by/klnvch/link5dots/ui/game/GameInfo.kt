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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
            modifier = Modifier.align(Alignment.TopEnd),
            infoViewState = GameInfoViewStateImpl(
                DotsStyleType.CROSS_AND_RING,
                "User 1",
                null
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
    Card(
        modifier = modifier.wrapContentSize()
    ) {
        UserRow(user1Dot, infoViewState.user1Name)
        UserRow(user2Dot, infoViewState.user2Name)
    }
}

@Composable
private fun UserRow(@DrawableRes dotResId: Int, name: String?) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = dotResId),
            contentDescription = null,
            tint = Color.Unspecified,
        )
        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = name ?: "",
            textAlign = TextAlign.Center,
        )
    }
}
