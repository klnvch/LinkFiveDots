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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;

import java.util.Locale;

import by.klnvch.link5dots.MainActivity;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.TwoPlayersActivity;

public class SettingsUtils {

    private static final String FIRST_RUN = "FIRST_RUN";
    private static final String APP_LANGUAGE = "APP_LANGUAGE";
    private static final String USER_NAME = "USER_NAME";
    private static final String IS_VIBRATION_ENABLED = "IS_VIBRATION_ENABLED";
    private static final String NIGHT_MODE = "NIGHT_MODE";

    public static long VIBRATE_DURATION = 500;

    public static boolean checkConfiguration(@NonNull Context context) {
        boolean isToBeRestated = false;
        // check language
        String savedLanguage = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(APP_LANGUAGE, null);
        if (savedLanguage != null) {
            Resources resources = context.getResources();
            String currentLanguage = resources.getConfiguration().locale.getLanguage();
            if (!savedLanguage.equals(currentLanguage)) {
                changeLanguage(context, savedLanguage);
                isToBeRestated = true;
            } else {
                isToBeRestated = false;
            }
        }
        // check night mode
        int nightMode = getNightMode(context);
        if (nightMode != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(nightMode);
            isToBeRestated = true;
        }
        //
        return isToBeRestated;
    }

    static void changeLanguage(@NonNull Context context, @NonNull String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        //
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(SettingsUtils.APP_LANGUAGE, language)
                .apply();
    }

    public static boolean isTheFirstRun(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(SettingsUtils.FIRST_RUN, true);
    }

    public static void setTheFirstRun(@NonNull Context context) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(SettingsUtils.FIRST_RUN, false)
                .apply();
    }

    @Nullable
    public static String getUserNameOrNull(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(USER_NAME, null);
    }

    @NonNull
    public static String getUserNameOrDefault(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(USER_NAME, context.getString(R.string.device_info_default));
    }

    static void setUserName(@NonNull Context context, @Nullable String username) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_NAME, username)
                .apply();
    }

    public static boolean isVibrationEnabled(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(IS_VIBRATION_ENABLED, true);
    }

    static void setVibrationMode(@NonNull Context context, boolean isVibrationEnabled) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(IS_VIBRATION_ENABLED, isVibrationEnabled)
                .apply();
    }

    private static int getNightMode(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    static void setNightMode(@NonNull Context context, int nightMode) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putInt(NIGHT_MODE, nightMode)
                .apply();
    }

    static void reset(@NonNull Context context) {
        // clear preferences
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().clear().apply();
        context.getSharedPreferences(MainActivity.class.getName(), Context.MODE_PRIVATE)
                .edit().clear().apply();
        context.getSharedPreferences(TwoPlayersActivity.class.getName(), Context.MODE_PRIVATE)
                .edit().clear().apply();

        // clear night mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // clear language
        Locale locale = Resources.getSystem().getConfiguration().locale;
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}