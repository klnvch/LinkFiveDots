package by.klnvch.link5dots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.ads.AdView;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.settings.UsernameDialog;

public class ScoresActivity extends AppCompatActivity {

    private AdView mAdView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AsyncTask<Void, Void, String> askForNameTask = null;
    private AsyncTask<Boolean, Void, JSONArray> doInternetJobTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (doInternetJobTask == null) {
                    doInternetJobTask = new DoInternetJob().execute(false);
                }
            }
        });

        // ads
        mAdView = App.initAds(this);

        // check name
        askForNameTask = new AskForName().execute();
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

        if (askForNameTask != null) {
            askForNameTask.cancel(true);
        }
        if (doInternetJobTask != null) {
            doInternetJobTask.cancel(true);
        }

        super.onDestroy();
    }

    private void updateRows(JSONArray result) throws JSONException {

        final String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        TableLayout tl = (TableLayout) findViewById(R.id.TableLayout111);

        tl.removeAllViews();

        for (int j = 0; j < result.length(); j++) {
            HighScore highScore = new HighScore(result.getJSONObject(j));

            final int color;
            if (deviceId.equals(highScore.getId())) {
                color = Color.RED;
            } else {
                color = Color.GREEN;
            }

            TableRow tr = new TableRow(this);

            TextView t0 = new TextView(this);
            t0.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.1f));
            t0.setWidth(0);
            t0.setText(String.format(Locale.getDefault(), "%d.", j + 1));
            t0.setTextColor(color);
            tr.addView(t0);

            TextView t1 = new TextView(this);
            t1.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.4f));
            t1.setWidth(0);
            t1.setText(highScore.getUserName());
            t1.setTextColor(color);
            tr.addView(t1);

            TextView t2 = new TextView(this);
            t2.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.3f));
            t2.setWidth(0);
            t2.setText(String.format(Locale.getDefault(), "%d", highScore.getScore()));
            t2.setTextColor(color);
            tr.addView(t2);

            TextView t3 = new TextView(this);
            t3.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.2f));
            t3.setWidth(0);
            t3.setText(String.format(Locale.getDefault(), "%d", highScore.getTime()));
            t3.setTextColor(color);
            tr.addView(t3);

            TextView t4 = new TextView(this);
            t4.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.3f));
            t4.setWidth(0);
            switch ((int) highScore.getStatus()) {
                case (int) HighScore.WON:
                    t4.setText(R.string.scores_won);
                    break;
                case (int) HighScore.LOST:
                    t4.setText(R.string.scores_lost);
                    break;
                default:
                    t4.setText("");
                    break;
            }
            t4.setTextColor(color);
            tr.addView(t4);

            tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scores_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.scores_update:
                if (doInternetJobTask == null) {
                    doInternetJobTask = new DoInternetJob().execute(false);
                }
                return true;
        }
        return false;
    }

    private void showAlertDialog() {

        UsernameDialog dialog = new UsernameDialog();
        getSupportFragmentManager()
                .beginTransaction()
                .add(dialog, UsernameDialog.TAG)
                .commitAllowingStateLoss();
        dialog.setOnUsernameChangeListener(new UsernameDialog.OnUsernameChangListener() {
            @Override
            public void onUsernameChanged(String newUserName) {
                doInternetJobTask = new DoInternetJob().execute(true);
            }

            @Override
            public void onNothingChanged() {
                doInternetJobTask = new DoInternetJob().execute(true);
            }
        });
    }

    private class AskForName extends AsyncTask<Void, Void, String> {

        private static final String TAG = "AskForName";

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                final String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
                URL url = new URL("http://link5dotsscores.appspot.com/get_user_name?id=" + deviceId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int response = conn.getResponseCode();

                if (response == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    return reader.readLine();
                } else {
                    Log.e(TAG, conn.getResponseMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            mSwipeRefreshLayout.setRefreshing(false);

            final String username = SettingsUtils.getUserName(ScoresActivity.this, null);

            if (result != null && !result.isEmpty()) {
                // there is some name in database

                if (username == null) {
                    // username not stored locally, save it
                    SettingsUtils.setUserName(ScoresActivity.this, result);
                    doInternetJobTask = new DoInternetJob().execute(false);
                } else if (username.equals(result)) {
                    // username remotely and locally are the same just go on
                    doInternetJobTask = new DoInternetJob().execute(false);
                } else {
                    // username remotely and locally are different, change it remotely
                    doInternetJobTask = new DoInternetJob().execute(true);
                }


            } else {
                // there is no name in database or some network error
                if (username == null) {
                    // we need username
                    showAlertDialog();
                } else {
                    // store username remotely
                    doInternetJobTask = new DoInternetJob().execute(true);
                }
            }

            askForNameTask = null;
        }
    }

    private class DoInternetJob extends AsyncTask<Boolean, Void, JSONArray> {

        private static final String TAG = "DoInternetJob";

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected JSONArray doInBackground(Boolean... params) {

            final boolean usernameHasChanged = params[0];

            //**************************************************************************************************
            // try to update user name in the data store
            //**************************************************************************************************
            try {
                if (usernameHasChanged) {
                    final String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
                    final String username = SettingsUtils.getUserName(ScoresActivity.this, getString(R.string.device_info_default));
                    URL url = new URL("http://link5dotsscores.appspot.com/update_high_scores?id=" + deviceId + "&newname=" + username);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int responseCode = conn.getResponseCode();

                    if (responseCode != 200) {
                        Log.e(TAG, conn.getResponseMessage());
                    }

                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            //**************************************************************************************************
            // try to upload current score to the data store
            //**************************************************************************************************
            try {
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    final String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
                    String username = SettingsUtils.getUserName(ScoresActivity.this, getString(R.string.device_info_default));
                    long gameStatus = getIntent().getExtras().getLong(HighScore.GAME_STATUS);
                    long numberOfMoves = getIntent().getExtras().getLong(HighScore.NUMBER_OF_MOVES);
                    long timeElapsed = getIntent().getExtras().getLong(HighScore.ELAPSED_TIME);

                    HighScore score = new HighScore(deviceId, username, numberOfMoves, timeElapsed, gameStatus);

                    StringBuilder fullUrl = new StringBuilder("http://link5dotsscores.appspot.com/");
                    fullUrl.append("add_high_scores?highscore=");

                    JSONObject jsonobject = score.toJSONObject();

                    fullUrl.append(URLEncoder.encode(jsonobject.toString(), "UTF-8"));

                    URL url = new URL(fullUrl.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int response = conn.getResponseCode();
                    if (response == 200) {
                        Log.i(TAG, "published successfully");
                    } else {
                        Log.e(TAG, conn.getResponseMessage());
                    }
                }
            } catch (Exception e) {
                if (e instanceof UnsupportedEncodingException) {
                    Log.e(TAG, e.getMessage());
                } else if (e instanceof MalformedURLException) {
                    Log.e(TAG, e.getMessage());
                } else if (e instanceof IOException) {
                    Log.e(TAG, e.getMessage());
                } else if (e instanceof JSONException) {
                    Log.e(TAG, e.getMessage());
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }

            //**************************************************************************************
            //try load best scores from data store
            //**************************************************************************************
            try {

                URL url = new URL("http://link5dotsscores.appspot.com/query_high_scores");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int response = conn.getResponseCode();

                if (response == 200) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String json = reader.readLine();
                    if (json.length() != 0) {
                        return new JSONArray(json);
                    }
                } else {
                    Log.e(TAG, conn.getResponseMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONArray result) {
            mSwipeRefreshLayout.setRefreshing(false);

            try {
                if (result != null) {
                    updateRows(result);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            doInternetJobTask = null;
        }
    }
}