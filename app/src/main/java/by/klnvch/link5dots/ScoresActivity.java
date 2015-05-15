package by.klnvch.link5dots;

import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class ScoresActivity extends Activity {

    private static final String USER_NAME = "USER_NAME";
    private static final String IS_USER_NAME_CHANGED = "IS_USER_NAME_CHANGED";
    private static final String IS_FIRST_RUN = "IS_FIRST_RUN";
	
	private final HttpClient client = new DefaultHttpClient();
	private ProgressDialog progressDialog = null;
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
		theFirstRun = prefs.getBoolean(IS_FIRST_RUN, true)
;		
		// prepare ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
		
		//set device id
		deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		
		// do Internet job
		launchProgressDialog();
		if(theFirstRun){
			askForName = new AskForName().execute(0);
		}else{
			doInternetJob = new DoInternetJob().execute(100);
		}
	}
	
	@Override
	protected void onDestroy() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(USER_NAME, username);
		editor.putBoolean(IS_USER_NAME_CHANGED, usernameHasChanged);
		editor.putBoolean(IS_FIRST_RUN, theFirstRun);
		editor.commit();
		
		super.onDestroy();
	}
	
	private void updateRows(JSONArray result) throws JSONException{
		
		TableLayout tl = (TableLayout)findViewById(R.id.TableLayout111);
		
		tl.removeAllViews();
		
		for (int j = 0; j < result.length(); j++) {
			HighScore highScore = new HighScore(result.getJSONObject(j));
			
			int color;
			if(deviceId.equals(highScore.getId())){
				color = Color.RED;
			}else{
				color = Color.GREEN;
			}
			
			TableRow tr = new TableRow(this);
			
			TextView t0 = new TextView(this);
			t0.setLayoutParams(new LayoutParams(-1,LayoutParams.WRAP_CONTENT, 0.1f));
			t0.setWidth(0);
			t0.setText(Integer.toString(j+1) + ".");
			t0.setTextColor(color);
			tr.addView(t0);
			
			TextView t1 = new TextView(this);
			t1.setLayoutParams(new LayoutParams(-1,LayoutParams.WRAP_CONTENT, 0.4f));
			t1.setWidth(0);
			t1.setText(highScore.getUserName());
			t1.setTextColor(color);
			tr.addView(t1);
			
			TextView t2 = new TextView(this);
			t2.setLayoutParams(new LayoutParams(-1,LayoutParams.WRAP_CONTENT, 0.3f));
			t2.setWidth(0);
			t2.setText(Long.toString(highScore.getScore()));
			t2.setTextColor(color);
			tr.addView(t2);
			
			TextView t3 = new TextView(this);
			t3.setLayoutParams(new LayoutParams(-1,LayoutParams.WRAP_CONTENT, 0.2f));
			t3.setWidth(0);
			t3.setText(Long.toString(highScore.getTime()));
			t3.setTextColor(color);
			tr.addView(t3);
			
			TextView t4 = new TextView(this);
			t4.setLayoutParams(new LayoutParams(-1,LayoutParams.WRAP_CONTENT, 0.3f));
			t4.setWidth(0);
			switch ((int)highScore.getStatus()) {
			case (int)HighScore.WON:
				t4.setText(R.string.scores_won);
				break;
			case (int)HighScore.LOST:
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
		inflater.inflate(R.menu.scoresmenu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()){
			case R.id.scores_edit:
				showAlertDialog();
				return true;
			case R.id.scores_update:
			
				launchProgressDialog();
				doInternetJob = new DoInternetJob().execute(100);
				return true;
		}
		return false;
	}
	
	private void showAlertDialog(){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.prompt_text);
        final EditText input = new EditText(this);
		input.setText(username);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
        builder.setView(input);
		builder.setPositiveButton(R.string.prompt_save, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				String tempUserName = input.getText().toString().replace(" ", "");
				if(!theFirstRun){
					if(!tempUserName.equals(username)){
						username = tempUserName;
						usernameHasChanged = true;
					}
				}else{
					username = tempUserName;
					theFirstRun = false;
					launchProgressDialog();
					doInternetJob = new DoInternetJob().execute(100);
				}
			}
		});
		builder.setNegativeButton(R.string.prompt_cancel, new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				if(theFirstRun){
					theFirstRun = false;
					launchProgressDialog();
					doInternetJob = new DoInternetJob().execute(100);
				}
			}
		});
		
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	private void launchProgressDialog(){
		progressDialog = ProgressDialog.show(ScoresActivity.this, "", getString(R.string.scores_loading));
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				if(askForName != null){
					askForName.cancel(true);
				}
				if(doInternetJob != null){
					doInternetJob.cancel(true);
				}
				ScoresActivity.this.onBackPressed();
			}
		});
	}
	
	private class AskForName extends AsyncTask<Integer, Integer, String>{
		@Override
		protected String doInBackground(Integer... params) {
			try{
                final String url = "http://link5dotsscores.appspot.com/get_user_name?id=" + deviceId;

				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				
				int statusCode = response.getStatusLine().getStatusCode();
				
				if(statusCode == 200){
					HttpEntity entity = response.getEntity();
					return EntityUtils.toString(entity);
				}else{
					//String reason = response.getStatusLine().getReasonPhrase();
					//drawMessage(statusCode + " - " + reason);
				}
			}catch (Exception e) {
                e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(final String result) {
				runOnUiThread(new Runnable() {
				
				public void run() {
					
					progressDialog.dismiss();
					
					if(result != null && !result.equals("")){
						username = result;
						theFirstRun = false;
						launchProgressDialog();
						doInternetJob = new DoInternetJob().execute(100);
					}else{
						showAlertDialog();
					}
				}
			});
		}
	}
	private class DoInternetJob extends AsyncTask<Integer, Integer, JSONArray>{
		@Override
		protected JSONArray doInBackground(Integer... params) {
			
			//**************************************************************************************************
			// try to update user name in the data store
			//**************************************************************************************************
			try{
				if(usernameHasChanged){

                    final String url = "http://link5dotsscores.appspot.com/update_high_scores?id=" + deviceId + "&newname=" + username;
					
					HttpGet get = new HttpGet(url);
					HttpResponse response = client.execute(get);
					
					int statusCode = response.getStatusLine().getStatusCode();
					
					if(statusCode == 200){
						usernameHasChanged = false;
					}else{
						//String reason = response.getStatusLine().getReasonPhrase();
						//drawMessage(statusCode + " - " + reason);
					}
					
				}
			}catch (Exception e) {
				//drawMessage(e.getMessage());
			}
			
			//**************************************************************************************************
			// try to upload current score to the data store
			//**************************************************************************************************
			try{
				Bundle bundle = getIntent().getExtras();
				if(bundle != null){
					long gameStatus = getIntent().getExtras().getLong("GAME_STATUS");
					long numberOfMoves = getIntent().getExtras().getLong("NUMBER_OF_MOVES");
					long timeElapsed = getIntent().getExtras().getLong("ELAPSED_TIME");
				
					HighScore score = new HighScore(deviceId, username, numberOfMoves, timeElapsed, gameStatus);
					
					StringBuilder fullUrl = new StringBuilder("http://link5dotsscores.appspot.com/");
					fullUrl.append("add_high_scores?highscore=");
						
					JSONObject jsonobject = score.toJSONObject();
					
					fullUrl.append(URLEncoder.encode(jsonobject.toString(), "UTF-8"));
					
					HttpGet get = new HttpGet(fullUrl.toString());
					HttpResponse response = client.execute(get);
					
					int statusCode = response.getStatusLine().getStatusCode();
					
					if(statusCode == 200){
					}else{
						//String reason = response.getStatusLine().getReasonPhrase();
						//drawMessage(statusCode + " - " + reason);
					}
				}
			}catch (Exception e) {
				//drawMessage(e.getMessage());
			}			
			
			//**************************************************************************************
			//try load best scores from data store
			//**************************************************************************************
			try{

                final String url = "http://link5dotsscores.appspot.com/query_high_scores";
				
				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				
				int statusCode = response.getStatusLine().getStatusCode();
				
				if(statusCode == 200){
					HttpEntity entity = response.getEntity();
					String json = EntityUtils.toString(entity);
					if(json.length() != 0){
						return new JSONArray(json);
					}
				}else{
					//String reason = response.getStatusLine().getReasonPhrase();
					//drawMessage(statusCode + " - " + reason);
				}
			}catch (Exception e) {
				//drawMessage(e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(final JSONArray result) {
			runOnUiThread(new Runnable() {
				
				public void run() {
					try{
						if(result!=null){
							updateRows(result);
						}
					}catch (Exception e) {
						//drawMessage(e.getMessage());
					}finally{
						progressDialog.dismiss();
					}
					
				}
			});
		}
	}
}
