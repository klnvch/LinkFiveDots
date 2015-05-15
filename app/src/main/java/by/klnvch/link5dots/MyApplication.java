package by.klnvch.link5dots;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

public class MyApplication extends Application{

    private Tracker tracker = null;

    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getTracker();
    }

    public synchronized Tracker getTracker() {

        if (tracker == null){
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            GoogleAnalytics.getInstance(this).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            tracker = analytics.newTracker(R.xml.tracker);
            tracker.enableAutoActivityTracking(true);
            tracker.enableExceptionReporting(true);
        }

        return tracker;
    }
}
