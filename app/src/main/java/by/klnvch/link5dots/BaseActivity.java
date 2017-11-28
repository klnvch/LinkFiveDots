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

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.settings.SettingsUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String KEY_GAME_STATE = "KEY_GAME_STATE_V1";
    protected static final String KEY_VIEW_STATE = "KEY_VIEW_STATE_V3";

    protected GameView mView;
    protected String mUserName = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);
        setTitle(R.string.app_name);

        mView = findViewById(R.id.game_view);
        mView.setOnMoveDoneListener(this::onMoveDone);
        mView.setOnGameEndListener(this::onGameFinished);

        setDots();

        Observable.fromCallable(this::getUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setUsername);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_offline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
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

    protected void undoLastMove() {
    }

    protected void newGame() {
        mView.resetGame();
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, null)
                .apply();
    }

    private void searchLastMove() {
        mView.switchHideArrow();
    }

    protected abstract void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot);

    protected abstract void onGameFinished(@NonNull HighScore highScore);

    protected void loadState() {
        Observable.fromCallable(this::getGameState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setGameState);

        Observable.fromCallable(this::getViewState)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setViewState);
    }

    protected void saveState() {
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, mView.getGameState().toJson())
                .putString(KEY_VIEW_STATE, mView.getViewState().toJson())
                .apply();
    }

    @NonNull
    private String getUserName() {
        return SettingsUtils.getUserNameOrDefault(this);
    }

    private void setUsername(@Nullable String name) {
        mUserName = name;
        TextView tvUsername = findViewById(R.id.text_user_name);
        tvUsername.setText(mUserName);
    }

    private void setDots() {
        GradientDrawable gameDotUser = (GradientDrawable) getResources()
                .getDrawable(R.drawable.game_dot);
        gameDotUser.setColor(Color.RED);
        TextView tvUserName = findViewById(R.id.text_user_name);
        tvUserName.setCompoundDrawablesWithIntrinsicBounds(
                gameDotUser, null, null, null);

        GradientDrawable gameDotOpponent = (GradientDrawable) getResources()
                .getDrawable(R.drawable.game_dot);
        gameDotOpponent.setColor(Color.BLUE);
        TextView tvOpponentName = findViewById(R.id.text_opponent_name);
        tvOpponentName.setCompoundDrawablesWithIntrinsicBounds(
                gameDotOpponent, null, null, null);
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