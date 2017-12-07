/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package by.klnvch.link5dots.multiplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import by.klnvch.link5dots.BaseActivity;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.dialogs.EndGameDialog;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.settings.SettingsUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public abstract class MultiplayerActivity extends BaseActivity {

    public static final String TAG = "MultiplayerActivity";

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String TOAST = "toast";
    private static final String KEY_ENEMY_NAME = "KEY_ENEMY_NAME";

    private final MHandler mHandler = new MHandler(this);
    private MultiplayerService mService;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MultiplayerService.LocalBinder binder = (MultiplayerService.LocalBinder) service;
            mService = binder.getService();
            mService.setHandler(mHandler);
            new Handler().postDelayed(this::sendNewGameMsg, 1000);
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        private void sendNewGameMsg() {
            sendMessage(new GameMessage(GameMessage.MSG_USERNAME, mUserName));
        }
    };
    private String mEnemyName = "";
    private boolean isVibrationEnabled = true;

    @NonNull
    protected abstract Class<?> getServiceClass();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.bt_message_your_turn);
        setEnemyName("-");

        Log.d(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(new Intent(this, getServiceClass()), mConnection, 0);

        Observable.fromCallable(this::getVibrationMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setVibrationMode);

        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String enemyName = savedInstanceState.getString(KEY_ENEMY_NAME);
        setEnemyName(enemyName);

        String jsonGameState = savedInstanceState.getString(KEY_GAME_STATE);
        mView.setGameState(Game.fromJson(jsonGameState));

        String jsonViewState = savedInstanceState.getString(KEY_VIEW_STATE);
        mView.setViewState(GameViewState.fromJson(jsonViewState));

        Log.d(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_ENEMY_NAME, mEnemyName);
        outState.putString(KEY_GAME_STATE, mView.getGameState().toJson());
        outState.putString(KEY_VIEW_STATE, mView.getViewState().toJson());
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mService != null) {
            unbindService(mConnection);
            mService = null;
        }

        Log.d(TAG, "onStop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_online, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mService != null && mService.getState() == MultiplayerService.STATE_CONNECTED) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.bluetooth_is_disconnect_question, mService.getDestinationName()))
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        mService.stop();
                        mService.start();
                        finish();
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void newGame() {
        mView.newGame(null);
        sendMessage(new GameMessage(GameMessage.MSG_NEW_GAME));
    }

    @Override
    protected void undoLastMove() {

    }

    @Override
    protected void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot) {
        if (previousDot == null || previousDot.getType() == Dot.OPPONENT) {
            // set user dot
            currentDot.setType(Dot.USER);
            mView.setDot(currentDot);
            //
            sendMessage(new GameMessage(GameMessage.MSG_DOT, currentDot));
            setTitle(R.string.bt_message_opponents_turn);
        }
    }

    @Override
    protected void onGameFinished(@NonNull HighScore highScore) {
        int title = highScore.getStatus() == HighScore.WON ? R.string.end_win : R.string.end_lose;
        String msg = getString(R.string.end_move, highScore.getMoves(), highScore.getTime());

        EndGameDialog.newInstance(msg, title, false)
                .setOnNewGameListener(this::newGame)
                .show(getSupportFragmentManager(), EndGameDialog.TAG);
    }

    private void setEnemyName(@Nullable String name) {
        mEnemyName = name;
        TextView tvOpponentName = findViewById(R.id.text_opponent_name);
        tvOpponentName.setText(mEnemyName);
    }

    private boolean getVibrationMode() {
        return SettingsUtils.isVibrationEnabled(this);
    }

    private void setVibrationMode(boolean isVibrationEnabled) {
        this.isVibrationEnabled = isVibrationEnabled;
    }

    private void sendMessage(@NonNull GameMessage msg) {
        // Check that we're actually connected before trying anything
        if (mService == null || mService.getState() != MultiplayerService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.bluetooth_disconnected, Toast.LENGTH_SHORT).show();
            return;
        }
        mService.write(msg.toJson());
    }

    private static class MHandler extends Handler {
        private final WeakReference<MultiplayerActivity> mActivity;

        MHandler(@NonNull MultiplayerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MultiplayerActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case MultiplayerService.STATE_CONNECTED:
                                activity.setTitle(R.string.bluetooth_connected);
                                activity.mView.newGame(null);
                                break;
                            case MultiplayerService.STATE_CONNECTING:
                                activity.setTitle(R.string.bluetooth_connecting);
                                break;
                            case MultiplayerService.STATE_LISTEN:
                            case MultiplayerService.STATE_NONE:
                                activity.setTitle(R.string.bluetooth_disconnected);
                                break;
                        }
                        break;
                    case MESSAGE_WRITE:
                        break;
                    case MESSAGE_READ:
                        GameMessage gameMessage = GameMessage.fromJson((String) msg.obj);
                        switch (gameMessage.getMsg()) {
                            case GameMessage.MSG_NEW_GAME:
                                activity.mView.newGame(null);
                                break;
                            case GameMessage.MSG_USERNAME:
                                String enemyName = (String) gameMessage.getObj();
                                activity.setEnemyName(enemyName);
                                break;
                            case GameMessage.MSG_DOT:
                                Dot dot = (Dot) gameMessage.getObj();
                                dot.setType(Dot.OPPONENT);
                                activity.mView.setDot(dot);
                                activity.setTitle(R.string.bt_message_your_turn);

                                if (activity.isVibrationEnabled) {
                                    Vibrator v = (Vibrator) activity
                                            .getSystemService(Context.VIBRATOR_SERVICE);
                                    if (v != null) {
                                        v.vibrate(SettingsUtils.VIBRATE_DURATION);
                                    }
                                }
                                break;
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
}