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

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.domain.models.NetworkUser;
import by.klnvch.link5dots.domain.models.RoomState;
import by.klnvch.link5dots.multiplayer.utils.OnRoomConnectedListener;

public class RoomConnectorTask {

    public static void connectRoom(@NonNull String roomKey,
                                   @NonNull NetworkUser user,
                                   @NonNull OnRoomConnectedListener listener) {
        checkNotNull(roomKey);
        checkNotNull(user);
        checkNotNull(listener);

        FirebaseHelper.getRoomReference(roomKey)
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                        final NetworkRoom room = currentData.getValue(NetworkRoom.class);
                        checkNotNull(room);
                        currentData.setValue(room.copy(room.getKey(), room.getTimestamp(), room.getDots(), room.getUser1(), user, room.getType(), RoomState.STARTED));
                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                        if (committed) {
                            final NetworkRoom room = currentData.getValue(NetworkRoom.class);
                            checkNotNull(room);

                            listener.onRoomConnected(room);
                        } else {
                            listener.onRoomConnectFailed(error.toException());
                        }
                    }
                });
    }
}