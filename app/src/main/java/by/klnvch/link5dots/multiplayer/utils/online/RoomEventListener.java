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
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.multiplayer.services.GameServiceOnline;
import by.klnvch.link5dots.multiplayer.utils.OnRoomUpdatedListener;

public class RoomEventListener implements ValueEventListener {

    private final DatabaseReference mDatabase;
    private final WeakReference<OnRoomUpdatedListener> mListener;
    private String mRoomKey = null;

    public RoomEventListener(@NonNull DatabaseReference database,
                             @NonNull OnRoomUpdatedListener listener) {
        mDatabase = database;
        mListener = new WeakReference<>(listener);
    }

    public void start(@NonNull String roomKey) {
        if (mRoomKey == null) {
            Log.d(GameServiceOnline.TAG, "RoomEventListener.start: ok");

            mRoomKey = roomKey;

            mDatabase
                    .child(Room.CHILD_ROOM)
                    .child(roomKey)
                    .addValueEventListener(this);

        } else if (mRoomKey.equals(roomKey)) {
            Log.e(GameServiceOnline.TAG, "RoomEventListener.start: same key");
        } else {
            Log.e(GameServiceOnline.TAG, "RoomEventListener.start: wrong key");
        }
    }

    public void stop(@NonNull String roomKey) {
        if (mRoomKey != null && mRoomKey.equals(roomKey)) {
            Log.d(GameServiceOnline.TAG, "RoomEventListener.stop: ok");

            mRoomKey = null;

            mDatabase
                    .child(Room.CHILD_ROOM)
                    .child(roomKey)
                    .removeEventListener(this);
        } else if (mRoomKey != null && !mRoomKey.equals(roomKey)) {
            Log.e(GameServiceOnline.TAG, "RoomEventListener.stop: wrong key");
        } else {
            Log.e(GameServiceOnline.TAG, "RoomEventListener.stop: null key");
        }
    }

    @Override
    public void onDataChange(DataSnapshot snapshot) {
        final OnRoomUpdatedListener listener = mListener.get();
        if (listener != null) {
            final Room room = snapshot.getValue(Room.class);
            if (room != null) {
                listener.onRoomUpdated(room, null);
            } else {
                listener.onRoomUpdated(null, new NullPointerException("room is null"));
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError error) {
        final OnRoomUpdatedListener listener = mListener.get();
        if (listener != null) {
            listener.onRoomUpdated(null, error.toException());
        }
    }
}