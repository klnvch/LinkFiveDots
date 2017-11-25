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
import android.view.View;
import android.widget.TextView;

import by.klnvch.link5dots.R;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isVibrationEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.settings_label);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Observable.fromCallable(this::getUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setUsername);

        Observable.fromCallable(this::getVibrationMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setVibrationMode);
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
                isVibrationEnabled = !isVibrationEnabled;
                SettingsUtils.setVibrationMode(this, isVibrationEnabled);
                setVibrationMode(isVibrationEnabled);
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
        isVibrationEnabled = SettingsUtils.isVibrationEnabled(this);
        return isVibrationEnabled;
    }

    private void setVibrationMode(boolean isVibrationEnabled) {
        TextView textView = findViewById(R.id.vibration_details);
        if (isVibrationEnabled) {
            textView.setText(R.string.switch_on_text);
        } else {
            textView.setText(R.string.switch_off_text);
        }
    }
}