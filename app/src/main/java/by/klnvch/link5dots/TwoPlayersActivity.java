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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.HighScore;

public class TwoPlayersActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.game_info).setVisibility(View.GONE);
    }

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
        if (getSupportFragmentManager().findFragmentByTag(EndGameDialog.TAG) != null) return;

        String msg = getString(R.string.end_move, highScore.getMoves(), highScore.getTime());

        EndGameDialog.newInstance(msg)
                .setOnNewGameListener(this::newGame)
                .setOnUndoMoveListener(this::undoLastMove)
                .show(getSupportFragmentManager(), EndGameDialog.TAG);
    }

    @Override
    protected void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        if (previousDot == null || previousDot.getType() == Dot.OPPONENT) {
            // set user dot
            currentDot.setType(Dot.USER);
            mView.setDot(currentDot);
        } else {
            currentDot.setType(Dot.OPPONENT);
            mView.setDot(currentDot);
        }
    }

    @Override
    protected void undoLastMove() {
        mView.undoLastMove(1);
    }
}