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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import by.klnvch.link5dots.models.Bot;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.scores.ScoresActivity;
import by.klnvch.link5dots.settings.SettingsUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_GAME_STATE = "KEY_GAME_STATE_V0";
    private static final String KEY_VIEW_STATE = "KEY_VIEW_STATE_V0";

    private GameView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);
        setTitle(R.string.app_name);

        mView = findViewById(R.id.game_view);
        mView.setOnMoveDoneListener(this::onMoveDone);
        mView.setOnGameEndListener(this::onGameFinished);

        Observable.fromCallable(this::getUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setUsername);

        FirebaseAuth.getInstance().signInAnonymously();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Observable.fromCallable(this::getGameState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setGameState);

        Observable.fromCallable(this::getViewState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setViewState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, mView.getGameState().toJson())
                .putString(KEY_VIEW_STATE, mView.getViewState().toJson())
                .apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_undo:
                undoLastMove();
                return true;
            case R.id.menu_new_game:
                newGame();
                return true;
            case R.id.menu_search:
                searchLastMove();
                return true;
        }
        return false;
    }

    @Override
    public boolean onSearchRequested() {
        searchLastMove();
        return true;
    }

    private void onGameFinished(@NonNull HighScore highScore) {
        int title = highScore.getStatus() == HighScore.WON ? R.string.end_win : R.string.end_lose;
        String msg = getString(R.string.end_move, highScore.getScore(), highScore.getTime());

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(R.string.end_new_game, (dialog, which) -> newGame())
                .setNeutralButton(R.string.scores_title, (dialog, which) -> moveToScores())
                .setNegativeButton(R.string.end_undo, (dialog, which) -> undoLastMove())
                .show();
    }

    private void undoLastMove() {
        mView.undoLastMove(2);
    }

    private void newGame() {
        mView.resetGame();
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, null)
                .apply();
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
        highScore.setAndroidId(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
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

    private void searchLastMove() {
        mView.switchHideArrow();
    }

    private void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        if (previousDot == null || previousDot.getType() == Dot.OPPONENT) {
            // set user dot
            currentDot.setType(Dot.USER);
            mView.setDot(currentDot);
            // set bot dot
            Dot botDot = Bot.findAnswer(mView.getCopyOfNet());
            botDot.setType(Dot.OPPONENT);
            mView.setDot(botDot);
        }
    }

    @NonNull
    private String getUserName() {
        return SettingsUtils.getUserNameOrDefault(this);
    }

    private void setUsername(@Nullable String username) {
        TextView tvUsername = findViewById(R.id.user_name);
        tvUsername.setText(username);
    }

    @NonNull
    private Game getGameState() {
        String jsonGameState = getPreferences(MODE_PRIVATE).getString(KEY_GAME_STATE, null);
        return Game.fromJson(jsonGameState);
    }

    private void setGameState(@NonNull Game game) {
        mView.setGameState(game);
    }

    @NonNull
    private GameViewState getViewState() {
        String jsonViewState = getPreferences(MODE_PRIVATE).getString(KEY_VIEW_STATE, null);
        return GameViewState.fromJson(jsonViewState);
    }

    private void setViewState(@NonNull GameViewState viewState) {
        mView.setViewState(viewState);
    }
}