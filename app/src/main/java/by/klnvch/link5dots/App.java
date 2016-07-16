package by.klnvch.link5dots;

import android.app.Application;
import android.content.res.Configuration;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import by.klnvch.link5dots.settings.SettingsUtils;

public class App extends Application {

    private Tracker mTracker = null;

    public App() {
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

        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.tracker);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
            if (BuildConfig.DEBUG) {
                analytics.setDryRun(true);
            }

            ExceptionReporter reporter = new ExceptionReporter(mTracker, Thread.getDefaultUncaughtExceptionHandler(), this);
            reporter.setExceptionParser(new AnalyticsExceptionParser(this, null));
            Thread.setDefaultUncaughtExceptionHandler(reporter);
        }

        return mTracker;
    }
}