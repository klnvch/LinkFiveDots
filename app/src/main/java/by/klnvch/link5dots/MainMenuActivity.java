package by.klnvch.link5dots;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainMenuActivity extends Activity implements OnClickListener{

    private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		
		findViewById(R.id.main_menu_single_player).setOnClickListener(this);
		findViewById(R.id.main_menu_multi_player).setOnClickListener(this);
		findViewById(R.id.main_menu_scores).setOnClickListener(this);
		findViewById(R.id.main_menu_how_to).setOnClickListener(this);
        findViewById(R.id.main_menu_info).setOnClickListener(this);

        // analytics
        //((MyApplication) getApplication()).getTracker();

        // ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
	}

    @Override
    protected void onStart() {
        super.onStart();
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
    protected void onStop() {
        super.onStop();
        //GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    public void onClick(View v) {
		
		switch (v.getId()) {
            case R.id.main_menu_single_player:
                Intent i1 = new Intent(this, MainActivity.class);
                startActivity(i1);
                break;
            case R.id.main_menu_multi_player:
                Intent i2 = new Intent(this, MultiPlayerMenuActivity.class);
                startActivity(i2);
                break;
            case R.id.main_menu_scores:
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()){
                    Intent i3 = new Intent(this, ScoresActivity.class);
                    startActivity(i3);
                }else{
                    Toast.makeText(this, R.string.scores_no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.main_menu_how_to:
                Intent i4 = new Intent(this, HowToActivity.class);
                startActivity(i4);
                break;
            case R.id.main_menu_info:
                Intent i5 = new Intent(this, InfoActivity.class);
                startActivity(i5);
                break;
		}
		
	}
}
