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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Random;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.utils.LinearCongruentialGenerator;

public class NewGameDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "NewGameDialog";

    private CheckBox mCheckBox;
    private EditText mEditText;

    private OnSeedNewGameListener mListener = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = View.inflate(getActivity(), R.layout.dialog_new_game, null);

        mCheckBox = v.findViewById(R.id.checkBoxSeed);
        mEditText = v.findViewById(R.id.editTextSeed);

        mCheckBox.setOnCheckedChangeListener((compoundButton, b) -> mEditText.setEnabled(b));
        int seed = new Random().nextInt(LinearCongruentialGenerator.MAX_SEED);
        String seedStr = Integer.toString(seed);
        mEditText.setText(seedStr);

        return new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setPositiveButton(R.string.okay, this)
                .setView(v)
                .create();
    }

    @Override
    public void onDestroy() {
        mListener = null;
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mCheckBox.isChecked()) {
                    String seedStr = mEditText.getText().toString();
                    Integer seed = Integer.parseInt(seedStr);
                    seed %= LinearCongruentialGenerator.MAX_SEED;
                    mListener.onSeedNewGame(seed);
                } else {
                    mListener.onSeedNewGame(null);
                }
                break;
        }
    }

    @NonNull
    public NewGameDialog setOnSeedNewGameListener(OnSeedNewGameListener listener) {
        this.mListener = listener;
        return this;
    }

    public interface OnSeedNewGameListener {
        void onSeedNewGame(@Nullable Integer seed);
    }
}