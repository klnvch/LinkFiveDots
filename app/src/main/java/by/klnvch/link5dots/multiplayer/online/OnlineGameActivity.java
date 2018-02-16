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

package by.klnvch.link5dots.multiplayer.online;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.models.Dot;

public class OnlineGameActivity extends AppCompatActivity
        implements OnlinePickerFragment.OnPickerListener, OnlineGameFragment.OnGameListener,
        FragmentManager.OnBackStackChangedListener {

    private static final String TAG = "OnlineGameActivity";

    private FirebaseAuth mAuth;
    private OnlineService mService;
    private OnlinePickerFragment mPickerFragment = null;
    private OnlineGameFragment mGameFragment = null;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            OnlineService.LocalBinder binder = (OnlineService.LocalBinder) service;
            mService = binder.getService();

            if (mPickerFragment != null) {
                mPickerFragment.setRoomState(mService.getRoomState(), mService.getRoom());
                mPickerFragment.setScanState(mService.getScanState(), mService.getAdapter());
            }

            if (mGameFragment != null) {
                mGameFragment.update();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");

            mService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        Log.d(TAG, "onCreate");

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> Log.d(TAG, "signInAnonymously: " + task.isSuccessful()));

        if (savedInstanceState == null) {
            mPickerFragment = new OnlinePickerFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, mPickerFragment, OnlinePickerFragment.TAG)
                    .commit();
        } else {
            mPickerFragment = (OnlinePickerFragment) getSupportFragmentManager()
                    .findFragmentByTag(OnlinePickerFragment.TAG);
            mGameFragment = (OnlineGameFragment) getSupportFragmentManager()
                    .findFragmentByTag(OnlineGameFragment.TAG);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        Intent intent = new Intent(this, OnlineService.class);
        if (savedInstanceState == null) {
            startService(intent);
        }
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        EventBus.getDefault().unregister(this);

        unbindService(mConnection);
        if (isFinishing()) {
            stopService(new Intent(this, OnlineService.class));
        }
        mService = null;

        mPickerFragment = null;
        mGameFragment = null;

        mAuth = null;
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

    @Override
    public void onBackPressed() {
        if (mGameFragment != null && mGameFragment.isVisible()) {
            final String msg = getString(R.string.bt_is_disconnect_question, getOpponentName());
            new AlertDialog.Builder(this)
                    .setMessage(msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> super.onBackPressed())
                    .setNegativeButton(R.string.no, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onBackStackChanged() {
        final int fragmentsCount = getSupportFragmentManager().getBackStackEntryCount();
        Log.d(TAG, "onBackStackChanged: " + fragmentsCount);
        if (fragmentsCount == 0) {
            // number of fragments in the stack decreased from 1 to 0,
            // expected quiting from Game Fragment to Picker Fragment
            mGameFragment = null;
            mService.reset();
        }
    }

    @Override
    public void onCreateRoom() {
        mService.createDestination();
    }

    @Override
    public void onDeleteRoom() {
        mService.deleteDestination();
    }

    @Override
    public void onStartScan() {
        mService.startScan();
    }

    @Override
    public void onStopScan() {
        mService.stopScan();
    }

    @Override
    public void onConnect(@NonNull Room room) {
        mService.connect(room);
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(@NonNull Integer event) {
        Log.d(TAG, "onMessageEvent: " + event);
        switch (event) {
            case OnlineService.STATE_ROOM_CREATING:
            case OnlineService.STATE_ROOM_CREATED:
            case OnlineService.STATE_ROOM_DELETING:
            case OnlineService.STATE_ROOM_DELETED:
                if (mPickerFragment != null && mService != null) {
                    mPickerFragment.setRoomState(event, mService.getCreatedDestination());
                }
                break;
            case OnlineService.STATE_SCAN_OFF:
            case OnlineService.STATE_SCAN_ON:
                if (mPickerFragment != null && mService != null) {
                    mPickerFragment.setScanState(event, mService.getAdapter());
                }
                break;
            case OnlineService.STATE_CONNECTED:
                if (mGameFragment != null) {
                    mGameFragment.update();
                } else {
                    mGameFragment = new OnlineGameFragment();
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragmentContainer, mGameFragment, OnlineGameFragment.TAG)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            case OnlineService.ERROR_CONNECT_FAILED:
                Toast.makeText(this,
                        getString(R.string.bluetooth_connecting_error_message, "null"),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Nullable
    @Override
    public String getUserName() {
        if (mService != null) {
            User user = mService.getUser();
            return user.getName();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getOpponentName() {
        if (mService != null) {
            Room room = mService.getRoom();
            User user = mService.getUser();
            if (user.equals(room.getUser1())) {
                return room.getUser2().getName();
            } else {
                return room.getUser1().getName();
            }
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public List<Dot> getDots() {
        if (mService != null) {
            Room room = mService.getRoom();
            return room.getDots();
        } else {
            return null;
        }
    }

    @Override
    public int getUserDotType() {
        if (mService != null) {
            if (mService.getUser().equals(mService.getRoom().getUser1()))
                return Dot.HOST;
            else
                return Dot.GUEST;
        } else {
            return Dot.EMPTY;
        }
    }


    @Override
    public void onMoveDone(@NonNull Dot dot) {
        mService.addDot(dot);
    }
}