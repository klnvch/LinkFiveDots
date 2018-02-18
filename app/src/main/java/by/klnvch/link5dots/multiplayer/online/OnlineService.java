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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.List;

import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.settings.SettingsUtils;

/*

    HOST                        GUEST

    INITIALIZATION PHASE:
    1. STATE_ROOM_DELETED       1. STATE_ROOM_DELETED
    2. STATE_SCAN_OFF           2. STATE_SCAN_OFF

    CONNECTING PHASE:
    3. STATE_ROOM_CREATING      3. STATE_SCAN_ON
    4. STATE_ROOM_CREATED

    PLAYING PHASE:
    5. STATE_CONNECTED          4. STATE_CONNECTED


 */
public class OnlineService extends Service {

    public static final String TAG = "OnlineGameService";

    static final int STATE_ROOM_DELETED = 0;
    static final int STATE_ROOM_CREATING = 1;
    static final int STATE_ROOM_CREATED = 2;
    static final int STATE_ROOM_DELETING = 3;

    static final int STATE_SCAN_OFF = 4;
    static final int STATE_SCAN_ON = 5;

    static final int STATE_CONNECTED = 6;
    static final int ERROR_CONNECT_FAILED = 7;

    private final IBinder mBinder = new LocalBinder();
    private DatabaseReference mDatabase = null;
    // must be initialized in onCreate
    private PickerAdapter mAdapter = null;
    private User mUser = null;
    // changed during existing
    private Room mRoom = null;
    private int mRoomState = STATE_ROOM_DELETED;
    private int mScanState = STATE_SCAN_OFF;
    private final ValueEventListener mStateEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Room room = dataSnapshot.getValue(Room.class);

            if (room != null) {
                switch (room.getState()) {
                    case Room.STATE_STARTED:
                        Log.d(TAG, "ValueEventListener.onDataChange: " + room.toString());

                        mRoom = room;
                        sendMsg(STATE_CONNECTED);
                        break;
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "ValueEventListener.onCancelled", databaseError.toException());
        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAdapter = PickerAdapter.createAdapter(mDatabase);
        mUser = new User(SettingsUtils.getUserNameOrDefault(this));

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

        deleteDestination();
        stopScan();

        super.onDestroy();
    }

    void createDestination() {
        setRoomState(STATE_ROOM_CREATING);

        // create destination
        String key = mDatabase.child(Room.CHILD_ROOM).push().getKey();
        mRoom = Room.newRoom(key, mUser);
        mDatabase
                .child(Room.CHILD_ROOM)
                .child(key)
                .setValue(mRoom)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createDestination: success");

                        startRoomObserving();

                        setRoomState(STATE_ROOM_CREATED);
                    } else {
                        Log.d(TAG, "createDestination", task.getException());
                        setRoomState(STATE_ROOM_DELETED);
                    }
                });
    }

    void deleteDestination() {
        if (mRoom == null) {
            setRoomState(STATE_ROOM_DELETED);
            return;
        }

        setRoomState(STATE_ROOM_DELETING);

        stopRoomObserving();

        // delete room
        mDatabase
                .child(Room.CHILD_ROOM)
                .child(mRoom.getKey())
                .child(Room.CHILD_STATE)
                .setValue(Room.STATE_DELETED)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deleteDestination: success");
                    } else {
                        Log.d(TAG, "deleteDestination", task.getException());
                    }

                    mRoom = null;

                    setRoomState(STATE_ROOM_DELETED);
                });
    }

    void startScan() {
        setScanState(STATE_SCAN_ON);

        mAdapter.startListening();
    }

    void stopScan() {
        setScanState(STATE_SCAN_OFF);

        mAdapter.stopListening();
    }

    void connect(@NonNull Room room) {
        mDatabase
                .child(Room.CHILD_ROOM)
                .child(room.getKey())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Room r = mutableData.getValue(Room.class);
                        if (r == null) return Transaction.abort();

                        r.setState(Room.STATE_STARTED);
                        r.setUser2(mUser);

                        mutableData.setValue(r);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        if (b) {
                            Log.d(TAG, "connect: success");

                            mRoom = dataSnapshot.getValue(Room.class);
                            startRoomObserving();
                            stopScan();

                            sendMsg(STATE_CONNECTED);
                        } else {
                            Log.d(TAG, "connect: " + databaseError);

                            sendMsg(ERROR_CONNECT_FAILED);
                        }
                    }
                });
    }

    void addDot(@NonNull Dot dot) {
        mDatabase
                .child(Room.CHILD_ROOM)
                .child(mRoom.getKey())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Room r = mutableData.getValue(Room.class);
                        if (r == null) return Transaction.abort();

                        List<Dot> dots = r.getDots();
                        if (dots != null) {
                            dots.add(dot);
                            r.setDots(dots);
                        } else {
                            r.setDots(Collections.singletonList(dot));
                        }

                        mutableData.setValue(r);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        if (b) {
                            Log.d(TAG, "connect: success");

                            mRoom = dataSnapshot.getValue(Room.class);
                            startRoomObserving();

                            sendMsg(STATE_CONNECTED);
                        } else {
                            Log.d(TAG, "connect: " + databaseError);

                            sendMsg(ERROR_CONNECT_FAILED);
                        }
                    }
                });
    }

    /**
     * Resets the service to initial state
     */
    void reset() {
        stopRoomObserving();
        mRoom = null;
        setRoomState(STATE_ROOM_DELETED);
    }

    private void startRoomObserving() {
        mDatabase
                .child(Room.CHILD_ROOM)
                .child(mRoom.getKey())
                .addValueEventListener(mStateEventListener);
    }

    private void stopRoomObserving() {
        mDatabase
                .child(Room.CHILD_ROOM)
                .child(mRoom.getKey())
                .removeEventListener(mStateEventListener);
    }

    private void sendMsg(int msg) {
        EventBus.getDefault().post(msg);
    }

    @Nullable
    Room getCreatedDestination() {
        return mRoom;
    }

    @NonNull
    PickerAdapter getAdapter() {
        return mAdapter;
    }

    @NonNull
    Room getRoom() {
        return mRoom;
    }

    @NonNull
    User getUser() {
        return mUser;
    }


    public int getRoomState() {
        return mRoomState;
    }

    private void setRoomState(int state) {
        this.mRoomState = state;
        sendMsg(state);
    }

    public int getScanState() {
        return mScanState;
    }

    private void setScanState(int state) {
        this.mScanState = state;
        sendMsg(state);
    }

    class LocalBinder extends Binder {
        OnlineService getService() {
            return OnlineService.this;
        }
    }
}