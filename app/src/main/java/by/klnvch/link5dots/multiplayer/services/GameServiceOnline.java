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

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.targets.TargetOnline;
import by.klnvch.link5dots.multiplayer.utils.online.RoomConnectorTask;
import by.klnvch.link5dots.multiplayer.utils.online.RoomCreatorTask;
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
    private RoomCreatorTask mRoomCreatorTask;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task ->
                        Log.d(TAG, "signInAnonymously: " + task.isSuccessful()));

        mRoomEventListener = new RoomEventListener();
        mRoomCreatorTask = new RoomCreatorTask();
    }

    @Override
    public void onDestroy() {
        mRoomCreatorTask.deleteRoom(null);
        mRoomEventListener.stop();
        super.onDestroy();
    }

    @Override
    public void createTarget() {
        super.createTarget();
        mRoomCreatorTask.createRoom(this, this, getUser());
    }

    @Override
    public void deleteTarget() {
        super.deleteTarget();
        mRoomCreatorTask.deleteRoom(this);
    }

    @Override
    public void connect(@NonNull Target target) {
        super.connect(target);

        final NetworkRoom room = ((TargetOnline) target).getTarget();
        RoomConnectorTask.connectRoom(room.getKey(), getUser(), this);
    }

    @Override
    public void newGame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        mRoomEventListener.stop();
        mRoomCreatorTask.finishRoom();
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
        final NetworkRoom room = ((TargetOnline) target).getTarget();
        setRoom(room);

        super.onTargetCreated(target);
    }

    @Override
    public void onTargetDeleted(@Nullable Exception exception) {
        setRoom(null);

        super.onTargetDeleted(exception);
    }

    @Override
    protected void updateRoomLocally(@NonNull NetworkRoom room) {
        sendMsg(room);
    }

    @Override
    protected void updateRoomRemotely(@NonNull NetworkRoom room) {
        UpdateRoomTask.updateRoom(this, room);
    }

    @Override
    protected void startGame(@Nullable NetworkRoom room) {
        checkNotNull(room);

        setRoom(room);
        mRoomEventListener.start(room.getKey(), this);
    }
}