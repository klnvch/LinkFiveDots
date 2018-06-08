package by.klnvch.link5dots.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.Locale;

public class LanguageUtils {

    @WorkerThread
    public static boolean checkLanguage(@NonNull Context context) {
        final String savedLanguage = getLanguage(context);
        if (savedLanguage != null) {
            final Resources resources = context.getResources();
            final String currentLanguage = resources.getConfiguration().locale.getLanguage();
            if (!savedLanguage.equals(currentLanguage)) {
                changeLanguage(context, savedLanguage);
                return true;
            }
        }
        return false;
    }

    @WorkerThread
    private static void changeLanguage(@NonNull Context context, @NonNull String language) {
        final Locale locale = new Locale(language);
        Locale.setDefault(locale);
        //
        final Resources resources = context.getResources();
        final Configuration config = new Configuration(resources.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        //
        setLanguage(context, language);
    }

    @Nullable
    private static String getLanguage(@NonNull Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(SettingsUtils.KEY_PREF_LANGUAGE, null);
    }

    @SuppressLint("ApplySharedPref")
    private static void setLanguage(@NonNull Context context, @Nullable String language) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(SettingsUtils.KEY_PREF_LANGUAGE, language)
                .commit();
    }
}
