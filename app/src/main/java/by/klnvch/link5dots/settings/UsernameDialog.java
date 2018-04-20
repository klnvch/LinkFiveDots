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

package by.klnvch.link5dots.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import by.klnvch.link5dots.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

public class UsernameDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "UsernameDialog";

    private OnUsernameChangedListener mListener = null;
    protected final CompositeDisposable mDisposables = new CompositeDisposable();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        checkNotNull(getContext());

        return new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setPositiveButton(R.string.okay, this)
                .setNegativeButton(R.string.cancel, null)
                .setView(View.inflate(getActivity(), R.layout.username, null))
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkNotNull(getContext());

        mDisposables.add(SettingsUtils.getUserNameOrDefault(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setUsername));
    }

    @Override
    public void onDestroy() {
        mListener = null;
        mDisposables.clear();
        super.onDestroy();
    }

    @NonNull
    public UsernameDialog setOnUsernameChangeListener(@NonNull OnUsernameChangedListener listener) {
        this.mListener = listener;
        return this;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        checkNotNull(getContext());

        final EditText editText = getDialog().findViewById(R.id.username);
        final String username = editText.getText().toString().trim();
        if (!username.isEmpty()) {
            SettingsUtils.setUserName(getContext(), username);
            if (mListener != null) {
                mListener.onUsernameChanged(username);
            }
        }
    }

    private void setUsername(@Nullable String username) {
        if (getDialog() != null) {
            EditText editText = getDialog().findViewById(R.id.username);
            editText.setText(username);
        } else {
            Log.e(TAG, "getDialog() returns null");
        }
    }

    public interface OnUsernameChangedListener {
        void onUsernameChanged(@NonNull String username);
    }
}