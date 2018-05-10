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

package by.klnvch.link5dots.multiplayer.utils.online;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;
import by.klnvch.link5dots.multiplayer.services.GameServiceOnline;
import by.klnvch.link5dots.multiplayer.targets.TargetOnline;
import by.klnvch.link5dots.multiplayer.utils.OnRoomConnectedListener;
import by.klnvch.link5dots.multiplayer.utils.OnTargetCreatedListener;
import by.klnvch.link5dots.multiplayer.utils.OnTargetDeletedListener;
import by.klnvch.link5dots.utils.RoomUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class RoomCreatorTask {

    private Room mRoom;
    private OnTargetCreatedListener mCreatedListener;
    private OnRoomConnectedListener mConnectedListener;

    private final ValueEventListener mStateEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            final Integer state = snapshot.getValue(Integer.class);
            checkNotNull(state);

            switch (state) {
                case Room.STATE_CREATED:
                    mCreatedListener.onTargetCreated(new TargetOnline(mRoom));
                    break;
                case Room.STATE_STARTED:
                    mConnectedListener.onRoomConnected(mRoom);
                    stopObserving();
                    break;
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            if (mCreatedListener != null) {
                mCreatedListener.onTargetCreationFailed(error.toException());
            }
        }
    };

    public void createRoom(@NonNull OnTargetCreatedListener createdListener,
                           @NonNull OnRoomConnectedListener connectedListener,
                           @NonNull User user) {
        checkNotNull(createdListener);
        checkNotNull(connectedListener);
        checkNotNull(user);
        checkState(mRoom == null);
        checkState(mCreatedListener == null);
        checkState(mConnectedListener == null);

        mCreatedListener = createdListener;
        mConnectedListener = connectedListener;

        final String key = FirebaseHelper.getKey();
        mRoom = RoomUtils.createOnlineGame(key, user);

        FirebaseHelper.getRoomReference(key)
                .setValue(mRoom)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startObserving();
                    } else {
                        final Exception exception = task.getException();
                        checkNotNull(exception);
                        createdListener.onTargetCreationFailed(exception);
                    }
                });
    }

    public void deleteRoom(@Nullable OnTargetDeletedListener listener) {
        if (mRoom != null) {
            checkNotNull(mCreatedListener);
            checkNotNull(mConnectedListener);

            mCreatedListener = null;
            mConnectedListener = null;

            stopObserving();

            FirebaseHelper.getStateReference(mRoom.getKey())
                    .setValue(Room.STATE_DELETED)
                    .addOnCompleteListener(task -> {
                        if (listener != null) listener.onTargetDeleted(task.getException());
                    });

            mRoom = null;
        } else {
            Log.w(GameServiceOnline.TAG, "already room deleted");
            checkState(mCreatedListener == null);
            checkState(mConnectedListener == null);
        }
    }

    private void startObserving() {
        FirebaseHelper.getStateReference(mRoom.getKey())
                .addValueEventListener(mStateEventListener);
    }

    private void stopObserving() {
        FirebaseHelper.getStateReference(mRoom.getKey())
                .removeEventListener(mStateEventListener);
    }
}