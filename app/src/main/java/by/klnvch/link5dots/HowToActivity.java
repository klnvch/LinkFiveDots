package by.klnvch.link5dots;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class HowToActivity extends AppCompatActivity {

    private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
		setContentView(R.layout.how_to);

        // ads
        // NullPointerException (@by.klnvch.link5dots.MainMenuActivity:onCreate:73) {main}
        // allow people to remove ads
        mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(App.DEVICE_ID_1)
                    .addTestDevice(App.DEVICE_ID_2)
                    .build();
            mAdView.loadAd(adRequest);
        }
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}