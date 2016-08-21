package by.klnvch.link5dots;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        findViewById(R.id.hello_user).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showUsernameDialog();
                return true;
            }
        });

        // check if first run
        new AsyncTask<Void, Void, Boolean>() {
            private SharedPreferences prefs;

            @Override
            protected Boolean doInBackground(Void... voids) {
                prefs = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
                return prefs.getBoolean(SettingsUtils.FIRST_RUN, true);
            }

            @Override
            protected void onPostExecute(Boolean isFirstRun) {
                if (isFirstRun) {
                    showUsernameDialog();
                    prefs.edit().putBoolean(SettingsUtils.FIRST_RUN, false).apply();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return SettingsUtils.getUserName(MainMenuActivity.this, null);
            }

            @Override
            protected void onPostExecute(String username) {
                TextView tvHelloUser = (TextView) findViewById(R.id.hello_user);
                if (username != null) {
                    tvHelloUser.setText(getString(R.string.greetings, username));
                    tvHelloUser.setVisibility(View.VISIBLE);
                } else {
                    tvHelloUser.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.main_menu_single_player:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.main_menu_multi_player:
                startActivity(new Intent(this, MultiPlayerMenuActivity.class));
                break;
            case R.id.main_menu_scores:
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    startActivity(new Intent(this, ScoresActivity.class));
                } else {
                    Toast.makeText(this, R.string.scores_no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.main_menu_how_to:
                startActivity(new Intent(this, HowToActivity.class));
                break;
            case R.id.main_menu_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
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
                TextView tvHelloUser = (TextView) findViewById(R.id.hello_user);
                tvHelloUser.setText(getString(R.string.greetings, username));
                tvHelloUser.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingChanged() {

            }
        });
    }
}