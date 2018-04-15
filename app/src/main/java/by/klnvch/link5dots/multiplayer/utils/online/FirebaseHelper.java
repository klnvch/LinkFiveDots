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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import by.klnvch.link5dots.BuildConfig;
import by.klnvch.link5dots.models.Room;

import static com.google.common.base.Preconditions.checkNotNull;

public class FirebaseHelper {
    private static final String CHILD_ROOM = BuildConfig.DEBUG ? "rooms_debug" : "rooms";
    private static final String CHILD_STATE = "state";

    public static boolean isSupported(@NonNull Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS;
    }

    @NonNull
    private static DatabaseReference getReference() {
        return FirebaseDatabase.getInstance().getReference().child(CHILD_ROOM);
    }

    @NonNull
    public static Query getRoomsQuery() {
        return getReference()
                .orderByChild(CHILD_STATE)
                .equalTo(Room.STATE_CREATED);
    }

    @NonNull
    public static String getKey() {
        return getReference().push().getKey();
    }

    @NonNull
    static DatabaseReference getRoomReference(@NonNull String roomKey) {
        checkNotNull(roomKey);

        return getReference().child(roomKey);
    }

    @NonNull
    static DatabaseReference getStateReference(@NonNull String roomKey) {
        checkNotNull(roomKey);

        return getRoomReference(roomKey).child(CHILD_STATE);
    }
}