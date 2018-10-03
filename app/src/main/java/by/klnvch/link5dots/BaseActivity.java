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
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import by.klnvch.link5dots.dialogs.NewGameDialog;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.utils.AnalyticsEvents;
import by.klnvch.link5dots.utils.RoomUtils;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseActivity extends DaggerAppCompatActivity
        implements GameFragment.OnGameListener, NewGameDialog.OnSeedNewGameListener {

    protected static final String KEY_GAME_STATE = "KEY_GAME_STATE_V6";
    protected final CompositeDisposable mDisposables = new CompositeDisposable();
    protected GameFragment mGameFragment;
    protected Room mRoom;
    protected FirebaseAnalytics mFirebaseAnalytics;
    @Inject
    SettingsUtils settingsUtils;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mGameFragment = (GameFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDisposables.add(Observable.fromCallable(this::loadRoom)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setRoom));
    }

    @Override
    protected void onStop() {
        if (mRoom != null) {
            getPreferences(MODE_PRIVATE).edit()
                    .putString(KEY_GAME_STATE, mRoom.toJson())
                    .apply();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mDisposables.clear();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game_offline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_undo:
                log(AnalyticsEvents.EVENT_UNDO_MOVE);
                undoLastMove();
                return true;
            case R.id.menu_new_game:
                log(AnalyticsEvents.EVENT_NEW_GAME);
                newGame();
                return true;
            case R.id.menu_search:
                log(AnalyticsEvents.EVENT_SEARCH);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSearchRequested() {
        log(AnalyticsEvents.EVENT_SEARCH);
        searchLastMove();
        return true;
    }

    protected abstract void undoLastMove();

    protected void newGame() {
        getPreferences(MODE_PRIVATE).edit()
                .putString(KEY_GAME_STATE, null)
                .apply();

        mGameFragment.update(RoomUtils.newGame(mRoom, null));

        new NewGameDialog().show(getSupportFragmentManager(), NewGameDialog.TAG);
    }

    @Override
    public void onSeedNewGame(@Nullable Long seed) {
        mGameFragment.update(RoomUtils.newGame(mRoom, seed));
    }

    private void searchLastMove() {
        mGameFragment.focus();
    }


    @NonNull
    private Room loadRoom() {
        final String json = getPreferences(MODE_PRIVATE).getString(KEY_GAME_STATE, null);
        final User user = User.newUser(settingsUtils.getUserNameOrDefault2());
        if (json != null) {
            final Room room = Room.fromJson(json);
            if (room.getUser1() != null) room.getUser1().setName(user.getName());
            return room;
        } else {
            return createRoom(user);
        }
    }

    private void setRoom(@NonNull Room room) {
        mGameFragment.update(mRoom = room);
    }

    private void log(@NonNull String event) {
        mFirebaseAnalytics.logEvent(event, null);
    }

    @NonNull
    protected abstract Room createRoom(@Nullable User host);
}