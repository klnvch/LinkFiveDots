package by.klnvch.link5dots.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.util.Locale;

public class SettingsUtils {

    public static final String APP_LANGUAGE = "APP_LANGUAGE";
    public static final String USER_NAME = "USER_NAME";
    public static final String FIRST_RUN = "FIRST_RUN";

    public static void checkLanguage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedLanguage = prefs.getString(APP_LANGUAGE, null);
        if (savedLanguage != null) {
            String currentLanguage = context.getResources().getConfiguration().locale.getDisplayName();
            if (!savedLanguage.equals(currentLanguage)) {
                Locale locale = new Locale(savedLanguage);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                context.getResources().updateConfiguration(config, null);
            }
        }
    }

    /**
     * get stored user name
     * @param context current context
     * @return user name or null
     */
    public static String getUserName(Context context, String defaultValue) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(USER_NAME, defaultValue);
    }

    public static void setUserName(Context context, String username) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_NAME, username)
                .apply();
    }
}
