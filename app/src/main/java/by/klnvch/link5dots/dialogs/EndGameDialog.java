/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

package by.klnvch.link5dots.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.analytics.FirebaseAnalytics;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.domain.models.GameResult;
import by.klnvch.link5dots.domain.models.GameScore;
import by.klnvch.link5dots.domain.repositories.Analytics;

public final class EndGameDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "EndGameDialog";

    private static final String KEY_IS_WON = "KEY_IS_WON";
    private static final String KEY_MOVES_NUMBER = "KEY_MOVES_NUMBER";
    private static final String KEY_ELAPSED_TIME = "KEY_ELAPSED_TIME";

    private OnNewGameListener mListenerNew = null;
    private OnUndoMoveListener mListenerUndo = null;
    private OnScoreListener mListenerScore = null;

    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Creates dialog showing the end of the game
     *
     * @param score        result of a game
     * @param ignoreResult ignore won or lost state of the game
     * @return dialog instance
     */
    @NonNull
    public static EndGameDialog newInstance(@NonNull GameScore score, boolean ignoreResult) {
        final Boolean isWon = ignoreResult ? null : score.getStatus() == GameResult.WON;
        final int movesNumber = score.getSize();
        final long elapsedTime = score.getDuration();

        final Bundle args = new Bundle();
        args.putSerializable(KEY_IS_WON, isWon);
        args.putInt(KEY_MOVES_NUMBER, movesNumber);
        args.putLong(KEY_ELAPSED_TIME, elapsedTime);

        final EndGameDialog dialog = new EndGameDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() == null || getActivity() == null) {
            throw new RuntimeException("arguments or context are null");
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), TAG, null);

        final Boolean isWon = (Boolean) getArguments().getSerializable(KEY_IS_WON);
        final int movesNumber = getArguments().getInt(KEY_MOVES_NUMBER);
        final long elapsedTime = getArguments().getLong(KEY_ELAPSED_TIME);

        final int title = isWon != null ? isWon ? R.string.end_win : R.string.end_lose : -1;
        final String timeStr = DateUtils.formatElapsedTime(elapsedTime);
        final String dotsSrt = Integer.toString(movesNumber);

        final View v = View.inflate(getContext(), R.layout.dialog_end_game, null);
        final TextView tvDuration = v.findViewById(R.id.textDurationValue);
        tvDuration.setText(timeStr);
        final TextView tvDots = v.findViewById(R.id.textDotsValue);
        tvDots.setText(dotsSrt);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v);

        if (title != -1) builder.setTitle(title);

        if (mListenerNew != null) builder.setPositiveButton(R.string.new_game, this);
        if (mListenerUndo != null) builder.setNegativeButton(R.string.undo, this);
        if (mListenerScore != null) builder.setNeutralButton(R.string.share, this);

        return builder.show();
    }

    @Override
    public void onDetach() {
        if (getActivity() != null) {
            mFirebaseAnalytics.setCurrentScreen(getActivity(), null, null);
        }
        mListenerNew = null;
        mListenerUndo = null;
        mListenerScore = null;
        super.onDetach();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mListenerNew != null) {
                    mFirebaseAnalytics.logEvent(Analytics.EVENT_NEW_GAME, null);
                    mListenerNew.onNewGame();
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if (mListenerUndo != null) {
                    mFirebaseAnalytics.logEvent(Analytics.EVENT_UNDO_MOVE, null);
                    mListenerUndo.onUndoMove();
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                if (mListenerScore != null) {
                    mFirebaseAnalytics.logEvent(Analytics.EVENT_PUBLISH_SCORE, null);
                    mListenerScore.onScore();
                }
                break;
        }
    }

    @NonNull
    public EndGameDialog setOnNewGameListener(@NonNull OnNewGameListener listenerNew) {
        this.mListenerNew = listenerNew;
        return this;
    }

    @NonNull
    public EndGameDialog setOnUndoMoveListener(@NonNull OnUndoMoveListener listenerUndo) {
        this.mListenerUndo = listenerUndo;
        return this;
    }

    @NonNull
    public EndGameDialog setOnScoreListener(@NonNull OnScoreListener listenerScore) {
        this.mListenerScore = listenerScore;
        return this;
    }

    public interface OnNewGameListener {
        void onNewGame();
    }

    public interface OnUndoMoveListener {
        void onUndoMove();
    }

    public interface OnScoreListener {
        void onScore();
    }
}