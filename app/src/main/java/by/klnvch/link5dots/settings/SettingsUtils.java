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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;

import java.lang.annotation.Retention;
import java.util.Locale;

import by.klnvch.link5dots.MainActivity;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.TwoPlayersActivity;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class SettingsUtils {

    public static final int DOTS_TYPE_ORIGINAL = 1;
    public static final int DOTS_TYPE_CROSS_AND_RING = 2;
    public static final long VIBRATE_DURATION = 500;
    private static final String FIRST_RUN = "FIRST_RUN";
    private static final String KEY_PREF_LANGUAGE = "pref_language";
    private static final String KEY_PREF_USERNAME = "pref_username";
    private static final String KEY_PREF_VIBRATION = "pref_vibration";
    private static final String KEY_NIGHT_MODE = "pref_night_mode";
    private static final String KEY_PREF_DOTS_TYPE = "pref_dots_type";

    public static boolean checkConfiguration(@NonNull Context context) {
        boolean isToBeRestated = false;
        // check language
        String savedLanguage = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_PREF_LANGUAGE, null);
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

    private static void changeLanguage(@NonNull Context context, @NonNull String language) {
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
                .putString(SettingsUtils.KEY_PREF_LANGUAGE, language)
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

    @NonNull
    public static String getUserNameOrEmpty(@NonNull Context context) {
        final String username = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_PREF_USERNAME, null);
        if (username == null) {
            return "";
        } else {
            return username;
        }
    }

    @NonNull
    public static String getUserNameOrDefault(@NonNull Context context) {
        final String username = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_PREF_USERNAME, null);
        if (username == null) {
            return context.getString(R.string.device_info_default);
        } else {
            return username;
        }
    }

    static void setUserName(@NonNull Context context, @Nullable String username) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_PREF_USERNAME, username)
                .apply();
    }

    public static boolean isVibrationEnabled(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(KEY_PREF_VIBRATION, true);
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

    @DotsType
    public static int getDotsType(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getInt(KEY_PREF_DOTS_TYPE, DOTS_TYPE_ORIGINAL);
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

    @Retention(SOURCE)
    @IntDef({DOTS_TYPE_ORIGINAL, DOTS_TYPE_CROSS_AND_RING})
    public @interface DotsType {
    }
}