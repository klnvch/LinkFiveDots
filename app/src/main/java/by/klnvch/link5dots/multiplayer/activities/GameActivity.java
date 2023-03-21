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

package by.klnvch.link5dots.multiplayer.activities;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import by.klnvch.link5dots.GameFragment;
import by.klnvch.link5dots.R;
import by.klnvch.link5dots.dialogs.EndGameDialog;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.multiplayer.adapters.TargetAdapterInterface;
import by.klnvch.link5dots.multiplayer.factories.FactoryActivityInterface;
import by.klnvch.link5dots.multiplayer.factories.FactoryProvider;
import by.klnvch.link5dots.multiplayer.services.GameService;
import by.klnvch.link5dots.multiplayer.services.GameServiceInterface;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.utils.GameState;
import by.klnvch.link5dots.utils.ActivityUtils;
import by.klnvch.link5dots.utils.RoomUtils;
import dagger.android.support.DaggerAppCompatActivity;

public abstract class GameActivity extends DaggerAppCompatActivity implements
        FragmentManager.OnBackStackChangedListener,
        PickerFragment.OnPickerListener,
        GameFragment.OnGameListener {

    private static final String TAG = "NsdGameActivity";
    protected GameFragment mGameFragment = null;
    GameServiceInterface mService = null; // can be null
    private FactoryActivityInterface mFactory;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            final GameService.LocalBinder binder = (GameService.LocalBinder) service;
            mService = binder.getService();

            final GameState state = mService.getState();
            onMessageEvent(state);

            if (mGameFragment != null) {
                final Room room = mService.getRoom();
                checkNotNull(room);
                onMessageEvent(room);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            mService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        mFactory = FactoryProvider.getActivityFactory(this.getClass());

        setTitle(mFactory.getDefaultTitle());

        if (savedInstanceState == null) {
            addPickerFragment();
        } else {
            mGameFragment = getGameFragment();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        final Intent intent = mFactory.getServiceIntent(this);
        if (savedInstanceState == null) {
            startService(intent);
        }
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);

        if (mFactory.isValid(this) && isValidFomMainMenuMoved()) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
            showErrorDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        EventBus.getDefault().unregister(this);

        try {
            unbindService(mConnection);
        } catch (Exception e) {
            Log.e(TAG, "onDestroy", e);
        }
        if (isFinishing()) {
            stopService(mFactory.getServiceIntent(this));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final GameFragment gameFragment = getGameFragment();

        if (gameFragment != null && gameFragment.isVisible()) {
            if (mService != null) {
                final Room room = mService.getRoom();
                if (room != null) {
                    final User anotherUser = RoomUtils.getAnotherUser(room, mService.getUser());

                    final String msg = getString(R.string.is_disconnect_question, anotherUser.getName());
                    new AlertDialog.Builder(this)
                            .setMessage(msg)
                            .setPositiveButton(R.string.yes, (dialog, which) -> super.onBackPressed())
                            .setNegativeButton(R.string.no, null)
                            .show();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        if (mService != null) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            final int fragmentsCount = fragmentManager.getBackStackEntryCount();
            Log.d(TAG, "onBackStackChanged: " + fragmentsCount);
            if (fragmentsCount == 0) {
                // number of fragments in the stack decreased from 1 to 0,
                // expected quiting from Game Fragment to Picker Fragment
                mGameFragment = null;
                mService.reset();
                setTitle(mFactory.getDefaultTitle());
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull Room room) {
        Log.d(TAG, "onMessageEvent: " + room);

        checkNotNull(mService);
        checkNotNull(mGameFragment);
        checkNotNull(room);

        mGameFragment.update(room);
        //
        // update title
        //
        final List<Dot> dots = room.getDots();
        if (dots == null || dots.size() == 0) {
            setTitle(R.string.bt_message_your_turn);
        } else {
            final int hostDotType =
                    mService.getUser().equals(room.getUser1()) ? Dot.HOST : Dot.GUEST;
            final Dot lastDot = dots.get(dots.size() - 1);

            if (lastDot.getType() == hostDotType) {
                setTitle(R.string.bt_message_opponents_turn);
            } else {
                setTitle(R.string.bt_message_your_turn);
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull Exception event) {
        Log.d(TAG, "onMessageEvent: " + event);
        // java.io.IOException: read failed, socket might closed or timeout, read ret: -1
        // java.net.ConnectException: failed to connect to /192.168.1.2 (port 47555): connect failed: EHOSTUNREACH (No route to host)
        // when disappeared
        showMsg();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull GameState state) {
        Log.d(TAG, "onMessageEvent: " + state);

        final PickerFragment pickerFragment = getPickerFragment();
        checkNotNull(pickerFragment);
        checkNotNull(state);

        pickerFragment.setState(state);

        //
        // update title
        //
        setTitle(mFactory.getDefaultTitle());

        switch (state.getTargetState()) {
            case GameState.STATE_TARGET_CREATING:
            case GameState.STATE_TARGET_DELETING:
                setTitle(R.string.connecting);
                break;
            case GameState.STATE_TARGET_CREATED:
                setTitle(R.string.progress_text);
                break;
            case GameState.STATE_NONE:
            case GameState.STATE_TARGET_DELETED:
                break;
        }

        switch (state.getScanState()) {
            case GameState.STATE_SCAN_ON:
                setTitle(R.string.searching);
                break;
            case GameState.STATE_NONE:
            case GameState.STATE_SCAN_DONE:
            case GameState.STATE_SCAN_OFF:
                break;
        }

        switch (state.getConnectState()) {
            case GameState.STATE_CONNECTED:
                setTitle(R.string.bt_message_your_turn);
                if (mGameFragment == null) {
                    mGameFragment = new GameFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragmentContainer, mGameFragment, GameFragment.TAG)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            case GameState.STATE_CONNECTING:
                setTitle(R.string.connecting);
                break;
            case GameState.STATE_DISCONNECTED:
                setTitle(R.string.disconnected);
                break;
            case GameState.STATE_NONE:
                break;
        }
    }

    @Override
    public void onCreateRoom() {
        if (mService != null) mService.createTarget();
    }

    @Override
    public void onDeleteRoom() {
        if (mService != null) mService.deleteTarget();
    }

    @Override
    public void onStartScan() {
        if (mService != null) mService.startScan();
    }

    @Override
    public void onStopScan() {
        if (mService != null) mService.stopScan();
    }

    @Override
    public void onConnect(@NonNull Target destination) {
        if (mService != null) mService.connect(destination);
    }

    @NonNull
    @Override
    public String getTargetLongName() {
        checkNotNull(mService.getTarget());
        return mService.getTarget().getLongName();
    }

    @NonNull
    @Override
    public TargetAdapterInterface getAdapter() {
        return mService.getAdapter();
    }

    @NonNull
    @Override
    public User getUser() {
        return mService.getUser();
    }

    @Override
    public void onMoveDone(@NonNull Dot dot) {
        mService.addDot(checkNotNull(dot));
    }

    @Override
    public void onGameFinished() {
        if (isFinishing()) return;

        checkNotNull(mService.getRoom());
        checkNotNull(mService.getUser());

        final HighScore highScore = RoomUtils.getHighScore(mService.getRoom(), mService.getUser());

        final EndGameDialog dialog = EndGameDialog.newInstance(highScore, false)
                .setOnNewGameListener(this::newGame);
        ActivityUtils.showDialog(getSupportFragmentManager(), dialog, EndGameDialog.TAG);
    }

    protected abstract void newGame();

    protected abstract boolean isValidFomMainMenuMoved();

    private void addPickerFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, mFactory.getPickerFragment(), PickerFragment.TAG)
                .commit();
    }

    @Nullable
    private PickerFragment getPickerFragment() {
        return (PickerFragment) getSupportFragmentManager().findFragmentByTag(PickerFragment.TAG);
    }

    @Nullable
    private GameFragment getGameFragment() {
        return (GameFragment) getSupportFragmentManager().findFragmentByTag(GameFragment.TAG);
    }

    private void showMsg() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.connect_failed)
                .setPositiveButton(R.string.okay, null)
                .show();
    }

    protected void showErrorDialog() {
        FirebaseCrashlytics.getInstance().log("feature not supported");
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.error_feature_not_available)
                .setPositiveButton(R.string.okay, (dialog, which) -> finish())
                .show();
    }
}
