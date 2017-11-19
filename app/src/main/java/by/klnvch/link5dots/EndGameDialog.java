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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class EndGameDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "EndGameDialog";

    private static final String KEY_TITLE = "KEY_TITLE";
    private static final String KEY_MSG = "KEY_MSG";
    private static final String KEY_CANCELABLE = "KEY_CANCELABLE";

    private OnNewGameListener listenerNew = null;
    private OnUndoMoveListener listenerUndo = null;
    private OnScoreListener listenerScore = null;

    public static EndGameDialog newInstance(@NonNull String msg, int title, boolean isCancelable) {
        Bundle args = new Bundle();
        args.putString(KEY_MSG, msg);
        args.putInt(KEY_TITLE, title);
        args.putBoolean(KEY_CANCELABLE, isCancelable);

        EndGameDialog dialog = new EndGameDialog();
        dialog.setArguments(args);
        return dialog;
    }

    public static EndGameDialog newInstance(@NonNull String msg) {
        Bundle args = new Bundle();
        args.putString(KEY_MSG, msg);

        EndGameDialog dialog = new EndGameDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String msg = getArguments().getString(KEY_MSG);
        int title = getArguments().getInt(KEY_TITLE);
        boolean isCancelable = getArguments().getBoolean(KEY_CANCELABLE, true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(isCancelable)
                .setMessage(msg);

        if (title != 0) builder.setTitle(title);

        if (listenerNew != null) builder.setPositiveButton(R.string.end_new_game, this);
        if (listenerUndo != null) builder.setNegativeButton(R.string.end_undo, this);
        if (listenerScore != null) builder.setNeutralButton(R.string.scores_title, this);

        return builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (listenerNew != null) {
                    listenerNew.onNewGame();
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if (listenerUndo != null) {
                    listenerUndo.onUndoMove();
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                if (listenerScore != null) {
                    listenerScore.onScore();
                }
                break;
        }
    }

    @NonNull
    public EndGameDialog setOnNewGameListener(OnNewGameListener listenerNew) {
        this.listenerNew = listenerNew;
        return this;
    }

    @NonNull
    public EndGameDialog setOnUndoMoveListener(OnUndoMoveListener listenerUndo) {
        this.listenerUndo = listenerUndo;
        return this;
    }

    @NonNull
    public EndGameDialog setOnScoreListener(OnScoreListener listenerScore) {
        this.listenerScore = listenerScore;
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