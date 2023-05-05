/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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
package by.klnvch.link5dots.ui.game.end

import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.BotGameScore
import by.klnvch.link5dots.domain.models.GameResult
import by.klnvch.link5dots.domain.models.GameScore
import by.klnvch.link5dots.domain.models.NetworkGameScore
import by.klnvch.link5dots.domain.models.SimpleGameScore
import by.klnvch.link5dots.utils.FormatUtils.formatDuration

data class EndGameViewState(
    val title: Int?,
    val size: String,
    val duration: String,
    val isShareable: Boolean,
    val isUndoMoveSupported: Boolean,
    val isNewGameSupported: Boolean,
) {
    constructor(
        score: GameScore,
        isUndoMoveSupported: Boolean,
        isNewGameSupported: Boolean
    ) : this(
        when (score) {
            is NetworkGameScore -> when (score.status) {
                GameResult.WON -> R.string.end_win
                GameResult.LOST -> R.string.end_lose
            }

            is BotGameScore -> when (score.status) {
                GameResult.WON -> R.string.end_win
                GameResult.LOST -> R.string.end_lose
            }

            is SimpleGameScore -> null
        },
        score.size.toString(),
        score.duration.formatDuration(),
        score is BotGameScore,
        isUndoMoveSupported,
        isNewGameSupported,
    )
}
