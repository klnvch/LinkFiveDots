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

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import by.klnvch.link5dots.scores.ScoresActivity;
import by.klnvch.link5dots.settings.InfoActivity;
import by.klnvch.link5dots.settings.SettingsActivity;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.settings.UsernameDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;

public class MainMenuActivity extends AppCompatActivity
        implements OnClickListener, View.OnLongClickListener {

    private static final int RC_SETTINGS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setTitle(R.string.app_name);

        FirebaseCrash.log("Activity created");

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        findViewById(R.id.hello_user).setOnLongClickListener(this);

        Observable.fromCallable(this::isTheFirstRun)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::checkTheFirstRun);

        Observable.fromCallable(this::checkConfiguration)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::changeConfiguration);

        Observable.fromCallable(this::getUserName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> "")
                .subscribe(this::setUsername);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_menu_single_player:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.main_menu_multi_player:
                startActivity(new Intent(this, MultiPlayerMenuActivity.class));
                break;
            case R.id.main_menu_scores:
                startActivity(new Intent(this, ScoresActivity.class));
                break;
            case R.id.main_menu_how_to:
                startActivity(new Intent(this, HowToActivity.class));
                break;
            case R.id.main_menu_about:
                startActivity(new Intent(this, InfoActivity.class));
                break;
            case R.id.main_menu_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class),
                        RC_SETTINGS);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SETTINGS:
                recreate();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.hello_user:
                showUsernameDialog();
                return true;
        }
        return false;
    }

    private void showUsernameDialog() {
        new UsernameDialog()
                .setOnUsernameChangeListener(this::setUsername)
                .show(getSupportFragmentManager(), null);
    }

    private boolean isTheFirstRun() {
        return SettingsUtils.isTheFirstRun(this);
    }

    private void checkTheFirstRun(boolean isTheFirstRun) {
        if (isTheFirstRun) {
            showUsernameDialog();
            SettingsUtils.setTheFirstRun(this);
        }
    }

    @Nullable
    private String getUserName() {
        return SettingsUtils.getUserNameOrNull(this);
    }

    private void setUsername(@NonNull String username) {
        TextView tvHelloUser = findViewById(R.id.hello_user);
        if (username.isEmpty()) {
            tvHelloUser.setVisibility(View.GONE);
        } else {
            tvHelloUser.setVisibility(View.VISIBLE);
            tvHelloUser.setText(getString(R.string.greetings, username));
        }
    }

    private boolean checkConfiguration() {
        return SettingsUtils.checkConfiguration(this);
    }

    private void changeConfiguration(boolean isLanguageChanged) {
        if (isLanguageChanged) {
            recreate();
        }
    }
}