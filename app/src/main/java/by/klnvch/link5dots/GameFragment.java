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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import by.klnvch.link5dots.db.RoomDao;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.utils.RoomUtils;
import dagger.android.support.DaggerFragment;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

public class GameFragment extends DaggerFragment {

    public static final String TAG = "GameFragment";
    private static final String KEY_VIEW_STATE = "KEY_VIEW_STATE";
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    @Inject
    public RoomDao roomDao;
    private GameView mView;
    private View mGameInfo;
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

        mGameInfo = root.findViewById(R.id.game_info);
        mTextUserName = root.findViewById(R.id.text_user_name);
        mTextOpponentName = root.findViewById(R.id.text_opponent_name);

        mView = root.findViewById(R.id.game_view);
        mView.setOnMoveDoneListener(this::onMoveDone);
        mView.setOnGameEndListener(this::onGameFinished);

        if (savedInstanceState != null) {
            mView.setViewState(GameViewState.fromJson(savedInstanceState.getString(KEY_VIEW_STATE)));
        }

        checkNotNull(getContext());
        mDisposables.add(SettingsUtils.getDotsType(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setDotsType));

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_VIEW_STATE, mView.getViewState().toJson());
        super.onSaveInstanceState(outState);
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
        inflater.inflate(R.menu.menu_game_fragment, menu);
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

        final User user = mListener.getUser();

        if (user != null) {
            mGameInfo.setVisibility(View.VISIBLE);

            final User user1 = room.getUser1();
            final User user2 = room.getUser2();

            final int hostDotType = RoomUtils.getHostDotType(room, user);

            if (hostDotType == Dot.HOST) {
                if (user1 != null) mTextUserName.setText(user1.getName());
                if (user2 != null) mTextOpponentName.setText(user2.getName());
            } else {
                if (user2 != null) mTextUserName.setText(user2.getName());
                if (user1 != null) mTextOpponentName.setText(user1.getName());
            }

            mView.setGameState(Game.createGame(room.getDots(), hostDotType));
        } else {
            mGameInfo.setVisibility(View.GONE);
            mView.setGameState(Game.createGame(room.getDots(), Dot.HOST));
        }

        // save to the db
        if (!RoomUtils.isEmpty(room)) {
            mDisposables.add(Completable.fromAction(() -> roomDao.insertRoom(room))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Log.d(TAG, "db success")));
        }
    }

    public void reset() {
        mView.newGame(null);
    }

    private void onMoveDone(@NonNull Dot dot) {
        mListener.onMoveDone(checkNotNull(dot));
    }

    private void onGameFinished(@NonNull HighScore highScore) {
        mListener.onGameFinished(checkNotNull(highScore));
    }

    public void focus() {
        mView.switchHideArrow();
    }

    public interface OnGameListener {
        @Nullable
        User getUser();

        void onMoveDone(@NonNull Dot dot);

        void onGameFinished(@NonNull HighScore highScore);
    }
}