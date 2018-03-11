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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import by.klnvch.link5dots.dialogs.NewGameDialog;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.utils.AnalyticsEvents;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String KEY_GAME_STATE = "KEY_GAME_STATE_V2";
    protected static final String KEY_VIEW_STATE = "KEY_VIEW_STATE_V3";
    protected final CompositeDisposable mDisposables = new CompositeDisposable();
    protected GameView mView;
    protected String mUserName = "";
    protected FirebaseAnalytics mFirebaseAnalytics;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);
        setTitle(R.string.app_name);

        mView = findViewById(R.id.game_view);
        mView.setOnMoveDoneListener(this::onMoveDone);
        mView.setOnGameEndListener(this::onGameFinished);

        mDisposables.add(Observable.fromCallable(() -> SettingsUtils.getUserNameOrDefault(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setUsername));

        mDisposables.add(Observable.fromCallable(() -> SettingsUtils.getDotsType(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setDotsType));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_offline, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        mDisposables.clear();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_undo:
                mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_UNDO_MOVE, null);
                undoLastMove();
                return true;
            case R.id.menu_new_game:
                mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_NEW_GAME, null);
                newGame();
                return true;
            case R.id.menu_search:
                mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_SEARCH, null);
                searchLastMove();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested() {
        mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_SEARCH, null);
        searchLastMove();
        return true;
    }

    protected abstract void undoLastMove();

    protected void newGame() {
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, null)
                .apply();

        mView.newGame(null);

        new NewGameDialog()
                .setOnSeedNewGameListener(mView::newGame)
                .show(getSupportFragmentManager(), NewGameDialog.TAG);
    }

    private void searchLastMove() {
        mView.switchHideArrow();
    }

    protected abstract void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot);

    protected abstract void onGameFinished(@NonNull HighScore highScore);

    protected void loadState() {
        mDisposables.add(Observable.fromCallable(() -> Game.fromJson(
                getPreferences(MODE_PRIVATE).getString(KEY_GAME_STATE, null)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mView::setGameState));

        mDisposables.add(Observable.fromCallable(() -> GameViewState.fromJson(
                getPreferences(MODE_PRIVATE).getString(KEY_VIEW_STATE, null)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mView::setViewState));
    }

    protected void saveState() {
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, mView.getGameState().toJson())
                .putString(KEY_VIEW_STATE, mView.getViewState().toJson())
                .apply();
    }

    private void setDotsType(int dotsType) {
        final TextView tvUserName = findViewById(R.id.text_user_name);
        final TextView tvOpponentName = findViewById(R.id.text_opponent_name);

        if (dotsType == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            tvUserName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.game_dot_circle_red, 0, 0, 0);
            tvOpponentName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.game_dot_circle_blue, 0, 0, 0);
        } else {
            tvUserName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.game_dot_cross_red, 0, 0, 0);
            tvOpponentName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.game_dot_ring_blue, 0, 0, 0);
        }
    }

    private void setUsername(@Nullable String name) {
        mUserName = name;
        final TextView tvUsername = findViewById(R.id.text_user_name);
        tvUsername.setText(mUserName);
    }
}