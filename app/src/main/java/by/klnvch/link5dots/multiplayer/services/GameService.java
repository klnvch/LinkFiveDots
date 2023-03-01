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

package by.klnvch.link5dots.multiplayer.services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.multiplayer.adapters.OnScanStoppedListener;
import by.klnvch.link5dots.multiplayer.adapters.ScannerInterface;
import by.klnvch.link5dots.multiplayer.adapters.TargetAdapterInterface;
import by.klnvch.link5dots.multiplayer.factories.FactoryProvider;
import by.klnvch.link5dots.multiplayer.factories.FactoryServiceInterface;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.utils.GameState;
import by.klnvch.link5dots.multiplayer.utils.OnRoomConnectedListener;
import by.klnvch.link5dots.multiplayer.utils.OnRoomUpdatedListener;
import by.klnvch.link5dots.multiplayer.utils.OnTargetCreatedListener;
import by.klnvch.link5dots.multiplayer.utils.OnTargetDeletedListener;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.utils.RoomUtils;
import dagger.android.DaggerService;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public abstract class GameService extends DaggerService implements GameServiceInterface,
        OnTargetCreatedListener, OnTargetDeletedListener,
        OnScanStoppedListener,
        OnRoomConnectedListener, OnRoomUpdatedListener {

    private static final String TAG = "GameService";
    protected final CompositeDisposable mDisposables = new CompositeDisposable();
    private final IBinder mBinder = new LocalBinder();
    private final GameState mState = new GameState();
    ScannerInterface mScanner;
    FactoryServiceInterface mFactory;
    @Inject
    SettingsUtils settingsUtils;
    private TargetAdapterInterface mAdapter;
    private User mUser;
    private Room mRoom = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        //
        mFactory = FactoryProvider.getServiceFactory(this.getClass());
        mAdapter = mFactory.getAdapter(this);
        mScanner = (ScannerInterface) mAdapter;

        // TODO: set NOT READY state till completion
        mDisposables.add(settingsUtils.getUserNameOrDefault()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> mUser = User.newUser(s)));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        mScanner.stopScan();
        mDisposables.clear();
        super.onDestroy();
    }

    @CallSuper
    @Override
    public void createTarget() {
        setRoomState(GameState.STATE_TARGET_CREATING);
    }

    @CallSuper
    @Override
    public void deleteTarget() {
        setRoomState(GameState.STATE_TARGET_DELETING);
    }

    @Override
    public void startScan() {
        setScanState(GameState.STATE_SCAN_ON);
        mScanner.startScan(this);
    }

    @Override
    public void stopScan() {
        setScanState(GameState.STATE_SCAN_OFF);
        mScanner.stopScan();
    }

    @CallSuper
    @Override
    public void connect(@NonNull Target target) {
        checkNotNull(target);
        setConnectState(GameState.STATE_CONNECTING);
    }

    @NonNull
    @Override
    public TargetAdapterInterface getAdapter() {
        return mAdapter;
    }

    @NonNull
    @Override
    public User getUser() {
        return mUser;
    }

    @Nullable
    @Override
    public Room getRoom() {
        return mRoom;
    }

    void setRoom(@Nullable Room mRoom) {
        this.mRoom = mRoom;
    }

    private void setRoomState(@GameState.TargetState int state) {
        mState.setTargetState(state);
        sendMsg(mState);
    }

    void setScanState(@GameState.ScanState int state) {
        mState.setScanState(state);
        sendMsg(mState);
    }

    void setConnectState(@GameState.ConnectState int state) {
        mState.setConnectState(state);
        sendMsg(mState);
    }

    @NonNull
    @Override
    public GameState getState() {
        return mState;
    }

    @Override
    public void addDot(@NonNull Dot dot) {
        if (mRoom != null) {
            updateRoomRemotely(RoomUtils.addDotMultiplayer(mRoom, mUser, dot));
        }
    }

    @CallSuper
    @Override
    public void reset() {
        Log.d(TAG, "reset");
        setConnectState(GameState.STATE_NONE);

        mRoom = null;
    }

    void sendMsg(@NonNull Exception exception) {
        EventBus.getDefault().post(exception);
    }

    private void sendMsg(@NonNull GameState state) {
        EventBus.getDefault().post(state);
    }

    void sendMsg(@NonNull Room room) {
        EventBus.getDefault().post(room);
    }

    protected abstract void updateRoomLocally(@NonNull Room room);

    protected abstract void updateRoomRemotely(@NonNull Room room);

    protected abstract void startGame(@Nullable Room room);

    @CallSuper
    @Override
    public void onTargetCreated(@NonNull Target target) {
        Log.d(TAG, "onTargetCreated");

        setRoomState(GameState.STATE_TARGET_CREATED);
    }

    @CallSuper
    @Override
    public void onTargetCreationFailed(@NonNull Exception exception) {
        Log.e(TAG, "onTargetCreationFailed: " + exception.getMessage());
        FirebaseCrashlytics.getInstance().recordException(exception);

        setRoomState(GameState.STATE_TARGET_DELETED);
    }

    @CallSuper
    @Override
    public void onTargetDeleted(@Nullable Exception exception) {
        Log.d(TAG, "onTargetDeleted: " + exception);
        setRoomState(GameState.STATE_TARGET_DELETED);
    }

    @CallSuper
    @Override
    public void onScanStopped(@Nullable Exception e) {
        Log.d(TAG, "onScanStopped: " + e);
        stopScan();
    }

    @Override
    public final void onRoomConnected(@Nullable Room room) {
        Log.d(TAG, "onRoomConnected");

        mScanner.stopScan();
        startGame(room);
        setConnectState(GameState.STATE_CONNECTED);
    }

    @Override
    public void onRoomConnectFailed(@NonNull Exception exception) {
        Log.w(TAG, "onRoomConnectFailed: " + exception.getMessage());

        FirebaseCrashlytics.getInstance().recordException(exception);
        setConnectState(GameState.STATE_NONE);
        sendMsg(exception);
    }

    @Override
    public void onRoomUpdated(@Nullable Room room, @Nullable Exception exception) {
        Log.d(TAG, "onRoomUpdated: " + exception);

        if (room != null && exception == null) {
            mRoom = room;
            updateRoomLocally(room);
        } else {
            if (exception != null) {
                FirebaseCrashlytics.getInstance().recordException(exception);
            }
            setConnectState(GameState.STATE_DISCONNECTED);
        }
    }

    public class LocalBinder extends Binder {
        public GameServiceInterface getService() {
            return GameService.this;
        }
    }
}
