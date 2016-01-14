package by.klnvch.link5dots;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import by.klnvch.link5dots.settings.SettingsUtils;

public class App extends Application {

    private static final String TAG = "App";

    private static final String DEVICE_ID_1 = "EA3A211E9E56D12855FE8A22E4EB356C";
    private static final String DEVICE_ID_2 = "EC37D6EC9A0387B1FC01F6EE89C228FC";
    private static final String DEVICE_ID_3 = "92A8239CCC0D5688305D260C32FD939A";
    //public static final String AD_UNIT_ID = "ca-app-pub-9653730523387780/5316470559";

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

    public static AdView initAds(Activity activity) {
        try {
            AdView mAdView = (AdView) activity.findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(App.DEVICE_ID_1)
                    .addTestDevice(App.DEVICE_ID_2)
                    .addTestDevice(App.DEVICE_ID_3)
                    .build();
            mAdView.loadAd(adRequest);
            return mAdView;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

    }
}