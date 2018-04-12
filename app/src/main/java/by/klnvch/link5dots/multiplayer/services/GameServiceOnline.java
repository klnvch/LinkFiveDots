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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.targets.TargetOnline;
import by.klnvch.link5dots.multiplayer.utils.online.ConnectRoomTask;
import by.klnvch.link5dots.multiplayer.utils.online.CreateRoomTask;
import by.klnvch.link5dots.multiplayer.utils.online.DeleteRoomTask;
import by.klnvch.link5dots.multiplayer.utils.online.RoomEventListener;
import by.klnvch.link5dots.multiplayer.utils.online.UpdateRoomTask;

import static com.google.common.base.Preconditions.checkNotNull;

/*

    HOST                        GUEST

    INITIALIZATION PHASE:
    1. STATE_TARGET_DELETED       1. STATE_TARGET_DELETED
    2. STATE_SCAN_OFF           2. STATE_SCAN_OFF

    CONNECTING PHASE:
    3. STATE_TARGET_CREATING      3. STATE_SCAN_ON
    4. STATE_TARGET_CREATED

    PLAYING PHASE:
    5. STATE_CONNECTED          4. STATE_CONNECTED


 */
public class GameServiceOnline extends GameService {

    public static final String TAG = "OnlineGameService";

    private RoomEventListener mRoomEventListener;
    private DatabaseReference mDatabase = null;

    @Override
    public void onCreate() {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        super.onCreate();

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task ->
                        Log.d(TAG, "signInAnonymously: " + task.isSuccessful()));

        mRoomEventListener = new RoomEventListener(mDatabase, this);
    }

    @Override
    public void onDestroy() {
        if (getRoom() != null) {
            DeleteRoomTask.deleteRoom(mDatabase, null, getRoom());
        }
        super.onDestroy();
    }

    @Override
    public void createTarget() {
        super.createTarget();
        CreateRoomTask.createRoom(mDatabase, this, Room.newRoom(getUser()));
    }

    @Override
    public void deleteTarget() {
        super.deleteTarget();
        final Room room = getRoom();

        checkNotNull(room);

        DeleteRoomTask.deleteRoom(mDatabase, this, room);
    }

    @Override
    public void connect(@NonNull Target target) {
        super.connect(target);

        final Room room = ((TargetOnline) target).getTarget();
        room.setUser2(getUser());

        ConnectRoomTask.connectRoom(mDatabase, this, room);
    }

    @Override
    public void newGame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        final Room room = getRoom();
        if (room != null) {
            mRoomEventListener.stop(getRoom().getKey());
        }

        super.reset();
    }

    @Nullable
    @Override
    public Target getTarget() {
        if (getRoom() != null) {
            return new TargetOnline(getRoom());
        } else {
            Log.e(TAG, "target is null");
            return null;
        }
    }

    @Override
    public void onTargetCreated(@NonNull Target target) {
        final Room room = ((TargetOnline) target).getTarget();
        setRoom(room);
        mRoomEventListener.start(room.getKey());

        super.onTargetCreated(target);
    }

    @Override
    public void onTargetDeleted(@Nullable Exception exception) {
        final Room room = getRoom();
        if (room != null) {
            setRoom(null);
            mRoomEventListener.stop(room.getKey());
        } else {
            Log.e(TAG, "onTargetDeleted: room is null");
        }

        super.onTargetDeleted(exception);
    }

    @Override
    protected void updateRoomLocally(@NonNull Room room) {
        if (room.getState() == Room.STATE_STARTED) {
            sendMsg(room);
        } else if (room.getUser1() != null && room.getUser2() != null) {
            onRoomConnected(room, null);

            room.setState(Room.STATE_STARTED);
            updateRoomRemotely(room);
        }
    }

    @Override
    protected void updateRoomRemotely(@NonNull Room room) {
        UpdateRoomTask.updateRoom(mDatabase, this, room);
    }

    @Override
    protected void startGame(@Nullable Room room) {
        if (room != null) {
            setRoom(room);
            mRoomEventListener.start(room.getKey());
        } else {
            throw new IllegalArgumentException();
        }
    }
}