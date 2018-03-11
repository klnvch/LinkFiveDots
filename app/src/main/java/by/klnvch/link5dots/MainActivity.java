/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
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

package by.klnvch.link5dots;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import by.klnvch.link5dots.dialogs.EndGameDialog;
import by.klnvch.link5dots.models.Bot;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.scores.ScoresActivity;
import by.klnvch.link5dots.utils.AnalyticsEvents;

public final class MainActivity extends BaseActivity {

    @Override
    protected void onResume() {
        super.onResume();
        loadState();
    }

    @Override
    protected void onPause() {
        saveState();
        super.onPause();
    }

    @Override
    protected void onGameFinished(@NonNull HighScore highScore) {
        mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_GAME_FINISHED, null);

        EndGameDialog.newInstance(highScore, false)
                .setOnNewGameListener(this::newGame)
                .setOnUndoMoveListener(this::undoLastMove)
                .setOnScoreListener(this::moveToScores)
                .show(getSupportFragmentManager(), EndGameDialog.TAG);
    }

    @Override
    protected void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_NEW_MOVE, null);

        if (previousDot == null || previousDot.getType() != Dot.HOST) {
            // set user dot
            mView.setHostDot(currentDot);
            // set bot dot
            final Dot botDot = Bot.findAnswer(mView.getGameState().getCopyOfNet());
            mView.setGuestDot(botDot);
        }
    }

    @Override
    protected void undoLastMove() {
        mView.undoLastMove(2);
    }

    private void moveToScores() {
        final HighScore highScore = mView.getHighScore();
        if (highScore != null) {
            final Intent intent = new Intent(this, ScoresActivity.class);
            intent.putExtra(HighScore.TAG, highScore);
            startActivity(intent);
        }
    }
}