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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import by.klnvch.link5dots.models.Bot;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.scores.ScoresActivity;
import by.klnvch.link5dots.settings.SettingsUtils;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth.getInstance().signInAnonymously();
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
        int title = highScore.getStatus() == HighScore.WON ? R.string.end_win : R.string.end_lose;
        String msg = getString(R.string.end_move, highScore.getScore(), highScore.getTime());

        EndGameDialog.newInstance(msg, title, true)
                .setOnNewGameListener(this::newGame)
                .setOnUndoMoveListener(this::undoLastMove)
                .setOnScoreListener(this::moveToScores)
                .show(getSupportFragmentManager(), EndGameDialog.TAG);
    }

    @Override
    protected void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        if (previousDot == null || previousDot.getType() == Dot.OPPONENT) {
            // set user dot
            currentDot.setType(Dot.USER);
            mView.setDot(currentDot);
            // set bot dot
            Dot botDot = Bot.findAnswer(mView.getGameState().getCopyOfNet());
            botDot.setType(Dot.OPPONENT);
            mView.setDot(botDot);
        }
    }

    @Override
    protected void undoLastMove() {
        mView.undoLastMove(2);
    }

    private void moveToScores() {
        HighScore highScore = mView.getHighScore();
        if (highScore != null) publishScore(highScore);
        startActivity(new Intent(this, ScoresActivity.class));
    }

    @SuppressLint("HardwareIds")
    private void publishScore(@NonNull HighScore highScore) {
        String userId;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            highScore.setUserId(userId);
        }
        highScore.setAndroidId(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
        highScore.setUserName(SettingsUtils.getUserNameOrNull(this));

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String key = mDatabase.child("high_scores").push().getKey();
        Map<String, Object> postValues = highScore.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/high_scores/" + key, postValues);
        mDatabase.updateChildren(childUpdates, (databaseError, databaseReference) -> {
            //Log.d(TAG, databaseError.getMessage());
        });
    }
}