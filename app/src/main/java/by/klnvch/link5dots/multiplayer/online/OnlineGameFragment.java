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

package by.klnvch.link5dots.multiplayer.online;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import by.klnvch.link5dots.GameView;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.dialogs.EndGameDialog;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.HighScore;

public class OnlineGameFragment extends Fragment {

    static final String TAG = "OnlineGameFragment";

    private GameView mView = null;
    private TextView mTextUserName;
    private TextView mTextOpponentName;
    private OnGameListener mListener;
    private int mHostDotType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGameListener) {
            mListener = (OnGameListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.game_board, container, false);

        mTextUserName = root.findViewById(R.id.text_user_name);
        mTextOpponentName = root.findViewById(R.id.text_opponent_name);

        mView = root.findViewById(R.id.game_view);
        mView.setOnMoveDoneListener(this::onMoveDone);
        mView.setOnGameEndListener(this::onGameFinished);

        setCardDots();
        update();

        return root;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private void setCardDots() {
        GradientDrawable gameDotUser = (GradientDrawable) getResources()
                .getDrawable(R.drawable.game_dot);
        gameDotUser.setColor(Color.RED);
        mTextUserName.setCompoundDrawablesWithIntrinsicBounds(
                gameDotUser, null, null, null);

        GradientDrawable gameDotOpponent = (GradientDrawable) getResources()
                .getDrawable(R.drawable.game_dot);
        gameDotOpponent.setColor(Color.BLUE);
        mTextOpponentName.setCompoundDrawablesWithIntrinsicBounds(
                gameDotOpponent, null, null, null);
    }

    void update() {
        // set right-top card
        if (mListener != null) {
            mTextUserName.setText(mListener.getUserName());
            mTextOpponentName.setText(mListener.getOpponentName());

            mHostDotType = mListener.getUserDotType();

            List<Dot> dots = mListener.getDots();
            if (mHostDotType != Dot.EMPTY) {
                mView.setGameState(Game.createGame(dots, mHostDotType));
            }
        } else {
            Log.e(TAG, "update: listener is null");
        }
    }

    private void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        if (previousDot == null || previousDot.getType() != mHostDotType) {
            Dot dot = mView.setHostDot(currentDot);

            if (dot != null) {
                mListener.onMoveDone(dot);
            } else {
                Log.e(TAG, "onMoveDone: result dot is null");
            }
        }
    }

    private void onGameFinished(@NonNull HighScore highScore) {
        int title = highScore.getStatus() == HighScore.WON ? R.string.end_win : R.string.end_lose;
        String msg = getString(R.string.end_move, highScore.getMoves(), highScore.getTime());

        if (getFragmentManager() != null) {
            EndGameDialog.newInstance(msg, title, false)
                    .setOnNewGameListener(this::newGame)
                    .show(getFragmentManager(), EndGameDialog.TAG);
        } else {
            Log.e(TAG, "getFragmentManager() is null");
        }

    }

    /**
     * Exits to Picker Fragment from Game Fragment
     */
    private void newGame() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }

    interface OnGameListener {
        @Nullable
        String getUserName();

        @Nullable
        String getOpponentName();

        @Nullable
        List<Dot> getDots();

        int getUserDotType();

        void onMoveDone(@NonNull Dot dot);
    }
}