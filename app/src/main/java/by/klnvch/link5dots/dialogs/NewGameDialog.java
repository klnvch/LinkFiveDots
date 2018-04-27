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

package by.klnvch.link5dots.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Random;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.utils.AnalyticsEvents;

public class NewGameDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "NewGameDialog";

    private OnSeedNewGameListener mListener = null;

    private FirebaseAnalytics mFirebaseAnalytics;

    private CheckBox mCheckBox = null;
    private EditText mEditText = null;

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new RuntimeException("activity is null");
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), TAG, null);

        final View v = View.inflate(getContext(), R.layout.dialog_new_game, null);

        mCheckBox = v.findViewById(R.id.checkBoxSeed);
        mEditText = v.findViewById(R.id.editTextSeed);

        mCheckBox.setOnCheckedChangeListener((compoundButton, b) -> mEditText.setEnabled(b));
        final long seed = new Random().nextInt(0xFFFF);
        String seedStr = Long.toString(seed);
        mEditText.setText(seedStr);

        return new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setPositiveButton(R.string.okay, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(v)
                .create();
    }

    @Override
    public void onDetach() {
        if (getActivity() != null) {
            mFirebaseAnalytics.setCurrentScreen(getActivity(), null, null);
        }
        mCheckBox.setOnCheckedChangeListener(null);
        mCheckBox = null;
        mEditText = null;
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (mListener == null) return;

        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mCheckBox.isChecked()) {
                    String seedStr = mEditText.getText().toString();
                    try {
                        final long seed = Long.parseLong(seedStr);
                        mListener.onSeedNewGame(seed);
                        mFirebaseAnalytics.logEvent(AnalyticsEvents.EVENT_GENERATE_GAME, null);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                } else {
                    mListener.onSeedNewGame(null);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mListener.onSeedNewGame(null);
                break;
        }
    }

    @NonNull
    public NewGameDialog setOnNewGameListener(@NonNull OnSeedNewGameListener listener) {
        this.mListener = listener;
        return this;
    }

    public interface OnSeedNewGameListener {
        void onSeedNewGame(@Nullable Long seed);
    }
}