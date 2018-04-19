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

package by.klnvch.link5dots.multiplayer.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import by.klnvch.link5dots.GameView;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.db.AppDatabase;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.settings.SettingsUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

public class GameFragment extends Fragment {

    public static final String TAG = "GameFragment";
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private GameView mView;
    private TextView mTextUserName;
    private TextView mTextOpponentName;
    private OnGameListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnGameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGameListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.game_board, container, false);

        mTextUserName = root.findViewById(R.id.text_user_name);
        mTextOpponentName = root.findViewById(R.id.text_opponent_name);

        mView = root.findViewById(R.id.game_view);
        mView.setOnMoveDoneListener(this::onMoveDone);
        mView.setOnGameEndListener(this::onGameFinished);

        checkNotNull(getContext());
        mDisposables.add(Observable.fromCallable(() -> SettingsUtils.getDotsType(getContext()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setDotsType));

        return root;
    }

    @Override
    public void onDestroyView() {
        mDisposables.clear();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_multiplayer_game, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                mView.switchHideArrow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDotsType(@SettingsUtils.DotsType int dotsType) {
        checkNotNull(getContext());

        mView.init(getContext(), dotsType);

        if (dotsType == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            setLeftDrawable(mTextUserName, R.drawable.game_dot_circle_red);
            setLeftDrawable(mTextOpponentName, R.drawable.game_dot_circle_blue);
        } else {
            setLeftDrawable(mTextUserName, R.drawable.game_dot_cross_red);
            setLeftDrawable(mTextOpponentName, R.drawable.game_dot_ring_blue);
        }
    }

    private void setLeftDrawable(@NonNull TextView view, @DrawableRes int drawable) {
        view.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
    }

    public void update(@NonNull Room room) {
        checkNotNull(room);
        checkNotNull(getContext());
        checkNotNull(mListener);

        final User user1 = room.getUser1();
        final User user2 = room.getUser2();

        final int hostDotType = room.getHostDotType(mListener.getUser());

        if (hostDotType == Dot.HOST) {
            mTextUserName.setText(user1.getName());
            mTextOpponentName.setText(user2.getName());
        } else {
            mTextUserName.setText(user2.getName());
            mTextOpponentName.setText(user1.getName());
        }

        mView.setGameState(Game.createGame(room.getDots(), hostDotType));

        // save to the db
        if (!room.isEmpty()) {
            mDisposables.add(Completable.fromAction(() ->
                    AppDatabase.getDB(getContext()).roomDao().insertRoom(room))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Log.d(TAG, "db success")));
        }
    }

    public void reset() {
        mView.newGame(null);
    }

    private void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        checkNotNull(currentDot);

        mListener.onMoveDone(currentDot);
    }

    private void onGameFinished(@NonNull HighScore highScore) {
        mListener.onGameFinished(highScore);
    }

    public interface OnGameListener {
        @NonNull
        User getUser();

        void onMoveDone(@NonNull Dot dot);

        void onGameFinished(@NonNull HighScore highScore);
    }
}