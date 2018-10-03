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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import java.lang.annotation.Retention;
import java.util.Locale;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import by.klnvch.link5dots.MainActivity;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.TwoPlayersActivity;
import by.klnvch.link5dots.utils.TestDevices;
import io.reactivex.Observable;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class SettingsUtils {

    public static final int DOTS_TYPE_ORIGINAL = 1;
    public static final int DOTS_TYPE_CROSS_AND_RING = 2;
    public static final long VIBRATE_DURATION = 500;
    public static final String KEY_PREF_LANGUAGE = "pref_language";
    private static final String FIRST_RUN = "FIRST_RUN";
    private static final String KEY_PREF_USERNAME = "pref_username";
    private static final String KEY_PREF_VIBRATION = "pref_vibration";
    private static final String KEY_NIGHT_MODE = "pref_night_mode";
    private static final String KEY_PREF_DOTS_TYPE = "pref_dots_type";

    private final Context mContext;

    public SettingsUtils(@NonNull Context context) {
        mContext = context;
    }

    public static Observable<Boolean> isConfigurationChanged(@NonNull Context context) {
        return Observable.fromCallable(() -> checkConfiguration(context));
    }

    private static boolean checkConfiguration(@NonNull Context context) {
        // check language
        boolean isToBeRestated = LanguageUtils.checkLanguage(context);
        // check night mode
        int nightMode = getNightMode(context);
        if (nightMode != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(nightMode);
            isToBeRestated = true;
        }
        //
        return isToBeRestated;
    }

    private static int getNightMode(@NonNull Context context) {
        final String nightMode = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_NIGHT_MODE, "unknown");
        switch (nightMode) {
            case "on":
                return AppCompatDelegate.MODE_NIGHT_YES;
            case "off":
                return AppCompatDelegate.MODE_NIGHT_NO;
            case "auto":
                return AppCompatDelegate.MODE_NIGHT_AUTO;
            case "system":
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }

    public Observable<Boolean> isTheFirstRun() {
        return Observable.fromCallable(() -> PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getBoolean(SettingsUtils.FIRST_RUN, true));
    }

    public void setTheFirstRun() {
        PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .edit()
                .putBoolean(SettingsUtils.FIRST_RUN, false)
                .apply();
    }

    @NonNull
    public Observable<String> getUserNameOrEmpty() {
        return Observable.fromCallable(() -> {
            final String username = PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .getString(KEY_PREF_USERNAME, null);
            if (username == null) {
                return "";
            } else {
                return username;
            }
        });
    }

    @NonNull
    public Observable<String> getUserNameOrDefault() {
        return Observable.fromCallable(this::getUserNameOrDefault2);
    }

    @NonNull
    public String getUserNameOrDefault2() {
        final String username = PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(KEY_PREF_USERNAME, null);
        if (username == null) {
            return mContext.getString(R.string.unknown);
        } else {
            return username;
        }
    }

    void setUserName(@Nullable String username) {
        PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .edit()
                .putString(KEY_PREF_USERNAME, username)
                .apply();
    }

    public Observable<Boolean> isVibrationEnabled() {
        return Observable.fromCallable(() -> PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getBoolean(KEY_PREF_VIBRATION, true));
    }

    public Observable<Integer> getDotsType() {
        return Observable.fromCallable(() -> PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getInt(KEY_PREF_DOTS_TYPE, DOTS_TYPE_ORIGINAL));
    }

    void reset() {
        // clear preferences
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit().clear().apply();
        mContext.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE)
                .edit().clear().apply();
        mContext.getSharedPreferences(TwoPlayersActivity.class.getName(), Context.MODE_PRIVATE)
                .edit().clear().apply();

        // clear night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // clear language
        Locale locale = Resources.getSystem().getConfiguration().locale;
        Locale.setDefault(locale);
        Resources resources = mContext.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @SuppressLint("HardwareIds")
    public boolean isTest() {
        final String androidID = Settings.Secure.getString(
                mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("SettingsUtils", "android id: " + androidID);
        return TestDevices.TEST_DEVICES.contains(androidID);
    }

    @Retention(SOURCE)
    @IntDef({DOTS_TYPE_ORIGINAL, DOTS_TYPE_CROSS_AND_RING})
    public @interface DotsType {
    }
}