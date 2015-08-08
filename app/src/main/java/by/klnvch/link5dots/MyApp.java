package by.klnvch.link5dots;

import android.app.Application;
import android.content.res.Configuration;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import by.klnvch.link5dots.settings.SettingsUtils;

public class MyApp extends Application {

    private Tracker tracker = null;

    public MyApp() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getTracker();
        //
        SettingsUtils.checkLanguage(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //
        SettingsUtils.checkLanguage(this);
    }

    public synchronized Tracker getTracker() {

        if (tracker == null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            tracker = analytics.newTracker(R.xml.tracker);
            tracker.enableAutoActivityTracking(true);
            tracker.enableExceptionReporting(true);
            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
            }
        }

        return tracker;
    }
}
