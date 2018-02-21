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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import by.klnvch.link5dots.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingsActivity";
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private boolean mIsVibrationEnabled = true;
    private boolean mIsNightMode = false;
    private int mDotsType = SettingsUtils.DOTS_TYPE_ORIGINAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings_label);
        setNightMode(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDisposables.add(Observable.fromCallable(this::getUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setUsername));

        mDisposables.add(Observable.fromCallable(this::getVibrationMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setVibrationMode));

        mDisposables.add(Observable.fromCallable(this::getDotsType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setDotsType));
    }

    @Override
    protected void onStop() {
        mDisposables.clear();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_username:
                new UsernameDialog()
                        .setOnUsernameChangeListener(this::setUsername)
                        .show(getSupportFragmentManager(), null);
                break;
            case R.id.settings_language:
                new LanguageDialog()
                        .setOnLanguageChangedListener(this::changeLanguage)
                        .show(getSupportFragmentManager(), null);
                break;
            case R.id.settings_vibration:
                mIsVibrationEnabled = !mIsVibrationEnabled;
                setVibrationMode(mIsVibrationEnabled);
                break;
            case R.id.settings_night_mode:
                mIsNightMode = !mIsNightMode;
                setNightMode(mIsNightMode);
                break;
            case R.id.settings_reset:
                SettingsUtils.reset(this);
                recreate();
                break;
            case R.id.settings_dots:
                mDotsType = mDotsType == SettingsUtils.DOTS_TYPE_ORIGINAL ?
                        SettingsUtils.DOTS_TYPE_CROSS_AND_RING : SettingsUtils.DOTS_TYPE_ORIGINAL;
                setDotsType(mDotsType);
                break;
        }
    }

    @NonNull
    private String getUserName() {
        return SettingsUtils.getUserNameOrDefault(this);
    }

    private void setUsername(@Nullable String username) {
        TextView tvUsername = findViewById(R.id.username_details);
        tvUsername.setText(username);
    }

    private void changeLanguage() {
        recreate();
    }

    private boolean getVibrationMode() {
        mIsVibrationEnabled = SettingsUtils.isVibrationEnabled(this);
        return mIsVibrationEnabled;
    }

    private void setVibrationMode(boolean isVibrationEnabled) {
        SettingsUtils.setVibrationMode(this, isVibrationEnabled);

        TextView textView = findViewById(R.id.vibration_details);
        if (isVibrationEnabled) {
            textView.setText(R.string.switch_on_text);
        } else {
            textView.setText(R.string.switch_off_text);
        }
    }

    private void setNightMode(boolean change) {
        int modeType = AppCompatDelegate.getDefaultNightMode();
        if (change) {
            switch (modeType) {
                case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                    modeType = AppCompatDelegate.MODE_NIGHT_AUTO;
                    break;
                case AppCompatDelegate.MODE_NIGHT_AUTO:
                    modeType = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case AppCompatDelegate.MODE_NIGHT_NO:
                    modeType = AppCompatDelegate.MODE_NIGHT_YES;
                    break;
                case AppCompatDelegate.MODE_NIGHT_YES:
                    modeType = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
            }
            AppCompatDelegate.setDefaultNightMode(modeType);
            getDelegate().applyDayNight();
            SettingsUtils.setNightMode(this, modeType);
        }
        Log.d(TAG, "Night mode: " + modeType);
        TextView textView = findViewById(R.id.details_night_mode);
        switch (modeType) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                textView.setText(R.string.settings_system);
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO:
                textView.setText(R.string.settings_auto);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                textView.setText(R.string.switch_off_text);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                textView.setText(R.string.switch_on_text);
                break;
        }
    }

    private int getDotsType() {
        mDotsType = SettingsUtils.getDotsType(this);
        return mDotsType;
    }

    private void setDotsType(int dotsType) {
        SettingsUtils.setDotsType(this, dotsType);

        final ImageView imageViewUserDot = findViewById(R.id.dots_user);
        final ImageView imageViewOpponentDot = findViewById(R.id.dots_opponent);
        if (dotsType == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            imageViewUserDot.setImageResource(R.drawable.game_dot_circle_red);
            imageViewOpponentDot.setImageResource(R.drawable.game_dot_circle_blue);
        } else {
            imageViewUserDot.setImageResource(R.drawable.game_dot_cross_red);
            imageViewOpponentDot.setImageResource(R.drawable.game_dot_ring_blue);
        }
    }
}