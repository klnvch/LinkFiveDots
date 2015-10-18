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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.ads.AdView;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import by.klnvch.link5dots.settings.UsernameDialog;

public class ScoresActivity extends AppCompatActivity {

    private static final String USER_NAME = "USER_NAME";
    private static final String IS_USER_NAME_CHANGED = "IS_USER_NAME_CHANGED";
    private static final String IS_FIRST_RUN = "IS_FIRST_RUN";
    private AdView mAdView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String deviceId;
    private String username;
    private boolean usernameHasChanged;
    private boolean theFirstRun;
    private AsyncTask<Integer, Integer, String> askForName = null;
    private AsyncTask<Integer, Integer, JSONArray> doInternetJob = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores);

        //restore data
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        username = prefs.getString(USER_NAME, getString(android.R.string.unknownName));
        usernameHasChanged = prefs.getBoolean(IS_USER_NAME_CHANGED, false);
        theFirstRun = prefs.getBoolean(IS_FIRST_RUN, true);

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                doInternetJob = new DoInternetJob().execute(100);
            }
        });

        // ads
        mAdView = App.initAds(this);

        //set device id
        deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        // do Internet job
        mSwipeRefreshLayout.setRefreshing(true);
        if (theFirstRun) {
            askForName = new AskForName().execute(0);
        } else {
            doInternetJob = new DoInternetJob().execute(100);
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

        if (askForName != null) {
            askForName.cancel(true);
        }
        if (doInternetJob != null) {
            doInternetJob.cancel(true);
        }

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(USER_NAME, username);
        editor.putBoolean(IS_USER_NAME_CHANGED, usernameHasChanged);
        editor.putBoolean(IS_FIRST_RUN, theFirstRun);
        editor.apply();

        super.onDestroy();
    }

    private void updateRows(JSONArray result) throws JSONException {

        TableLayout tl = (TableLayout) findViewById(R.id.TableLayout111);

        tl.removeAllViews();

        for (int j = 0; j < result.length(); j++) {
            HighScore highScore = new HighScore(result.getJSONObject(j));

            int color;
            if (deviceId.equals(highScore.getId())) {
                color = Color.RED;
            } else {
                color = Color.GREEN;
            }

            TableRow tr = new TableRow(this);

            TextView t0 = new TextView(this);
            t0.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.1f));
            t0.setWidth(0);
            t0.setText(Integer.toString(j + 1) + ".");
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
            t2.setText(Long.toString(highScore.getScore()));
            t2.setTextColor(color);
            tr.addView(t2);

            TextView t3 = new TextView(this);
            t3.setLayoutParams(new LayoutParams(-1, LayoutParams.WRAP_CONTENT, 0.2f));
            t3.setWidth(0);
            t3.setText(Long.toString(highScore.getTime()));
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
                    t4.setText("internal error");
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
                mSwipeRefreshLayout.setRefreshing(true);
                doInternetJob = new DoInternetJob().execute(100);
                return true;
        }
        return false;
    }

    private void showAlertDialog() {

        UsernameDialog dialog = new UsernameDialog();
        dialog.show(getSupportFragmentManager(), null);
        dialog.setOnUsernameChangeListener(new UsernameDialog.OnUsernameChangListener() {
            @Override
            public void onUsernameChanged(String tempUserName) {
                tempUserName = tempUserName.replace(" ", "");
                if (!theFirstRun) {
                    if (!tempUserName.equals(username)) {
                        username = tempUserName;
                        usernameHasChanged = true;
                    }
                } else {
                    username = tempUserName;
                    theFirstRun = false;
                    mSwipeRefreshLayout.setRefreshing(true);
                    doInternetJob = new DoInternetJob().execute(100);
                }
            }

            @Override
            public void onNothingChanged() {
                if (theFirstRun) {
                    theFirstRun = false;
                    mSwipeRefreshLayout.setRefreshing(true);
                    doInternetJob = new DoInternetJob().execute(100);
                }
            }
        });
    }

    private class AskForName extends AsyncTask<Integer, Integer, String> {

        private static final String TAG = "AskForName";

        @Override
        protected String doInBackground(Integer... params) {
            try {

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
        protected void onPostExecute(final String result) {
            runOnUiThread(new Runnable() {

                public void run() {

                    mSwipeRefreshLayout.setRefreshing(false);

                    if (result != null && !result.equals("")) {
                        username = result;
                        theFirstRun = false;
                        mSwipeRefreshLayout.setRefreshing(true);
                        new DoInternetJob().execute(100);
                    } else {
                        showAlertDialog();
                    }
                }
            });

            askForName = null;
        }
    }

    private class DoInternetJob extends AsyncTask<Integer, Integer, JSONArray> {

        private static final String TAG = "DoInternetJob";

        @Override
        protected JSONArray doInBackground(Integer... params) {

            //**************************************************************************************************
            // try to update user name in the data store
            //**************************************************************************************************
            try {
                if (usernameHasChanged) {

                    URL url = new URL("http://link5dotsscores.appspot.com/update_high_scores?id=" + deviceId + "&newname=" + username);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int responseCode = conn.getResponseCode();

                    if (responseCode == 200) {
                        usernameHasChanged = false;
                    } else {
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
            runOnUiThread(new Runnable() {

                public void run() {
                    try {
                        if (result != null) {
                            updateRows(result);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    } finally {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                }
            });

            doInternetJob = null;
        }
    }
}