package by.klnvch.link5dots.bluetooth;

import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import by.klnvch.link5dots.GameView;
import by.klnvch.link5dots.HighScore;
import by.klnvch.link5dots.Dot;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.settings.SettingsUtils;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothActivity extends AppCompatActivity {

    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothService Handler
    public static final String TOAST = "toast";

    private GameView view;
    private AlertDialog alertDialog = null;

    private static final String MESSAGE_CLOSE_END_ACTIVITY = "CLOSE_END_ACTIVITY";
    private static final String MESSAGE_USERNAME = "MESSAGE_USERNAME";
    private static final String PREFERENCE_OPPONENT_USERNAME = "PREFERENCE_OPPONENT_USERNAME";

    private final MHandler mHandler = new MHandler(this);

    private BluetoothService mBluetoothService;

    private String userName = "";
    private String enemyName = "";

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothService.setHandler(mHandler);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendMessage(MESSAGE_USERNAME + userName);
                }
            }, 1000);
        }

        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);

        setTitle(R.string.bt_message_your_turn);

        // my initialization
        view = (GameView)findViewById(R.id.game_view);

        view.setOnGameEventListener(new GameView.OnGameEventListener() {
            @Override
            public void onMoveDone(Dot currentDot, Dot previousDot) {
                // set user dot
                currentDot.setType(Dot.USER);
                view.setDot(currentDot);
                //
                sendMessage(currentDot.toString());
                setTitle(R.string.bt_message_opponents_turn);
            }
            @Override
            public void onGameEnd(HighScore highScore) {
                showEndAlertDialog(highScore);
            }
        });

        userName = SettingsUtils.getUserName(this);
        if (userName != null) {
            TextView tvUsername = (TextView)findViewById(R.id.user_name);
            tvUsername.setText(userName);
        }
        TextView tvOpponentName = (TextView)findViewById(R.id.opponent_name);
        tvOpponentName.setText("-");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, 0);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        enemyName = savedInstanceState.getString(PREFERENCE_OPPONENT_USERNAME);
        TextView tvOpponentName = (TextView)findViewById(R.id.opponent_name);
        tvOpponentName.setText(enemyName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //
        SharedPreferences prefs = getSharedPreferences(BluetoothService.BLUETOOTH_GAME_VIEW_PREFERENCES, MODE_PRIVATE);
        view.restore(prefs);
        view.invalidate();
        view.isOver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
        SharedPreferences prefs = getSharedPreferences(BluetoothService.BLUETOOTH_GAME_VIEW_PREFERENCES, MODE_PRIVATE);
        view.save(prefs);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //
        if(alertDialog != null){
            alertDialog.cancel();
        }
        // Unbind from the service
        if (mBluetoothService != null) {
            unbindService(mConnection);
            mBluetoothService = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PREFERENCE_OPPONENT_USERNAME, enemyName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if(mBluetoothService != null && mBluetoothService.getState() == BluetoothService.STATE_CONNECTED){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.bluetooth_is_disconnect_question, mBluetoothService.getDeviceName()));
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    mBluetoothService.stop();
                    mBluetoothService.start();
                    finish();
                }
            });
            builder.setNegativeButton(R.string.no, null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothService == null || mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.bluetooth_disconnected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBluetoothService.write(send);

        }
    }

    private void showEndAlertDialog(HighScore highScore){
        if(alertDialog == null || !alertDialog.isShowing()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            if (highScore.getStatus() == HighScore.WON) {
                builder.setTitle(R.string.end_win);
            } else {
                builder.setTitle(R.string.end_lose);
            }
            String str = getString(R.string.end_move, highScore.getScore(), highScore.getTime());
            builder.setMessage(str);
            builder.setNeutralButton(R.string.end_new_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    sendMessage(MESSAGE_CLOSE_END_ACTIVITY);
                    view.resetGame();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }
    
    // The Handler that gets information back from the BluetoothService
    private static class MHandler extends Handler{
        private final WeakReference<BluetoothActivity> mActivity;
 		 
        public MHandler(BluetoothActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
 	 
        @Override
        public void handleMessage(Message msg) {
            BluetoothActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                activity.setTitle(R.string.bluetooth_connected);
                                activity.view.resetGame();
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                activity.setTitle(R.string.bluetooth_connecting);
                                break;
                            case BluetoothService.STATE_LISTEN:
                            case BluetoothService.STATE_NONE:
                                activity.setTitle(R.string.bluetooth_disconnected);
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        break;
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[])msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        if(readMessage.equals(MESSAGE_CLOSE_END_ACTIVITY)) {
                            activity.view.resetGame();
                        } else if(readMessage.startsWith(MESSAGE_USERNAME)) {
                            activity.enemyName = readMessage.replace(MESSAGE_USERNAME, "");
                            TextView opponentName = (TextView)activity.findViewById(R.id.opponent_name);
                            opponentName.setText(activity.enemyName);
                        } else {
                            Dot dot = Dot.parseString(readMessage);
                            dot.setType(Dot.OPPONENT);
                            activity.view.setDot(dot);
                            activity.setTitle(R.string.bt_message_your_turn);
                        }
                        break;
                    case MESSAGE_TOAST:
                        Toast.makeText(activity.getApplicationContext(),
                                msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    public boolean onSearchRequested() {
        view.switchHideArrow();
        return true;
    }
}