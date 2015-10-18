package by.klnvch.link5dots;

import com.google.android.gms.ads.AdView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import by.klnvch.link5dots.settings.SettingsActivity;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.settings.UsernameDialog;

public class MainMenuActivity extends AppCompatActivity implements OnClickListener {

    private static final int SETTINGS_REQUEST_CODE = 3;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
		
        findViewById(R.id.main_menu_single_player).setOnClickListener(this);
        findViewById(R.id.main_menu_multi_player).setOnClickListener(this);
        findViewById(R.id.main_menu_scores).setOnClickListener(this);
        findViewById(R.id.main_menu_how_to).setOnClickListener(this);
        findViewById(R.id.main_menu_settings).setOnClickListener(this);

        findViewById(R.id.hello_user).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showUsernameDialog();
                return true;
            }
        });

        // check if first run
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = prefs.getBoolean(SettingsUtils.FIRST_RUN, true);
        if (isFirstRun) {
            showUsernameDialog();
            prefs.edit().putBoolean(SettingsUtils.FIRST_RUN, false).apply();
        }

        // ads
        // NullPointerException (@by.klnvch.link5dots.MainMenuActivity:onCreate:73) {main}
        // allow people to remove ads
        mAdView = App.initAds(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        //
        String username = SettingsUtils.getUserName(this, null);
        TextView tvHelloUser = (TextView) findViewById(R.id.hello_user);
        if (username != null) {
            tvHelloUser.setText(getString(R.string.greetings, username));
            tvHelloUser.setVisibility(View.VISIBLE);
        } else {
            tvHelloUser.setVisibility(View.GONE);
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

    @Override
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
            case R.id.main_menu_settings:
                Intent i5 = new Intent(this, SettingsActivity.class);
                startActivityForResult(i5, SETTINGS_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_REQUEST_CODE:
                finish();
                startActivity(getIntent());
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showUsernameDialog() {
        UsernameDialog dialog = new UsernameDialog();
        dialog.show(getSupportFragmentManager(), null);
        dialog.setOnUsernameChangeListener(new UsernameDialog.OnUsernameChangListener() {
            @Override
            public void onUsernameChanged(String username) {
                TextView tvHelloUser = (TextView)findViewById(R.id.hello_user);
                tvHelloUser.setText(getString(R.string.greetings, username));
                tvHelloUser.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingChanged() {

            }
        });
    }
}