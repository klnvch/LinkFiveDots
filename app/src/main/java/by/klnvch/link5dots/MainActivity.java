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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import by.klnvch.link5dots.dialogs.EndGameDialog;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.scores.ScoresActivity;
import by.klnvch.link5dots.utils.AnalyticsEvents;
import by.klnvch.link5dots.utils.RoomUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
    }

    @Nullable
    @Override
    public User getUser() {
        return mRoom.getUser1();
    }

    @Override
    public void onMoveDone(@NonNull Dot dot) {
        mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_NEW_MOVE, null);

        mGameFragment.update(RoomUtils.addDotWithBotAnswer(mRoom, dot));
    }

    @Override
    public void onGameFinished(@NonNull HighScore highScore) {
        mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_GAME_FINISHED, null);

        EndGameDialog.newInstance(highScore, false)
                .setOnNewGameListener(this::newGame)
                .setOnUndoMoveListener(this::undoLastMove)
                .setOnScoreListener(() -> moveToScores(highScore))
                .show(getSupportFragmentManager(), EndGameDialog.TAG);
    }

    @Override
    protected void undoLastMove() {
        mGameFragment.update(RoomUtils.undo(RoomUtils.undo(mRoom)));
    }

    @NonNull
    @Override
    protected Room createRoom(@Nullable User host) {
        checkNotNull(host);

        return RoomUtils.createBotGame(host, User.newUser(getString(R.string.android)));
    }

    private void moveToScores(@NonNull HighScore highScore) {
        checkNotNull(getUser());

        highScore.setUserName(getUser().getName());
        final Intent intent = new Intent(this, ScoresActivity.class);
        intent.putExtra(HighScore.TAG, highScore);
        startActivity(intent);
    }
}