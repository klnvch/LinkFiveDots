package by.klnvch.link5dots;

import android.app.Application;
import android.content.res.Configuration;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import by.klnvch.link5dots.settings.SettingsUtils;

public class App extends Application {

    public static final String DEVICE_ID_1 = "EA3A211E9E56D12855FE8A22E4EB356C";
    public static final String DEVICE_ID_2 = "EC37D6EC9A0387B1FC01F6EE89C228FC";
    public static final String AD_UNIT_ID = "ca-app-pub-9653730523387780/5316470559";

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
            GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
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
