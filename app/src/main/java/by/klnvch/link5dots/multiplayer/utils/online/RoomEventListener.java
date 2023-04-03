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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.multiplayer.services.GameServiceOnline;
import by.klnvch.link5dots.multiplayer.utils.OnRoomUpdatedListener;

public class RoomEventListener implements ValueEventListener {

    private OnRoomUpdatedListener mListener = null;
    private String mRoomKey = null;

    public void start(@NonNull String roomKey,
                      @NonNull OnRoomUpdatedListener listener) {
        checkNotNull(roomKey);
        checkNotNull(listener);

        if (mRoomKey == null) {
            mRoomKey = roomKey;
            mListener = listener;
            FirebaseHelper.getRoomReference(roomKey)
                    .addValueEventListener(this);
        } else {
            checkState(mRoomKey.equals(roomKey));
            Log.w(GameServiceOnline.TAG, "already listening for updates");
        }
    }

    public void stop() {
        if (mRoomKey != null) {
            checkNotNull(mListener);

            FirebaseHelper.getRoomReference(mRoomKey)
                    .removeEventListener(this);

            mListener = null;
            mRoomKey = null;
        } else {
            checkState(mListener == null);
            Log.w(GameServiceOnline.TAG, "already listening stopped");
        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (mListener != null) {
            final NetworkRoom room = snapshot.getValue(NetworkRoom.class);
            checkNotNull(room);

            mListener.onRoomUpdated(room, null);
        } else {
            Log.w(GameServiceOnline.TAG, "update listener is null");
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        if (mListener != null) {
            mListener.onRoomUpdated(null, error.toException());
        } else {
            Log.w(GameServiceOnline.TAG, "update listener is null");
        }
    }
}