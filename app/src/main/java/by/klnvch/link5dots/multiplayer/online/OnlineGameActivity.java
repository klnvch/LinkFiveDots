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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import by.klnvch.link5dots.R;

public class OnlineGameActivity extends AppCompatActivity implements OnDataActionListener {

    private static final String TAG = "OnlineGame";
    private final Map<String, Room> mRoomsMap = new HashMap<>();
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new OnlineStartFragment())
                    .commit();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void createRoom() {
        String key = mDatabase.child("rooms").push().getKey();
        mDatabase.child("rooms").child(key).setValue(Room.dummy()).addOnCompleteListener(task -> Log.d(TAG, "onComplete")).addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess")).addOnFailureListener(e -> Log.d(TAG, "onFailure"));
    }

    @Override
    public void findRooms(@NonNull OnUpdateListListener listener) {
        Log.d(TAG, "findRooms");
        mRoomsMap.clear();

        mDatabase.child("rooms")
                .orderByChild("state")
                .equalTo(1)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Room room = dataSnapshot.getValue(Room.class);
                        mRoomsMap.put(dataSnapshot.getKey(), room);
                        listener.onUpdateReady(mRoomsMap);
                        Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey() + ", " + room + ", " + s);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildChanged");
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Room room = dataSnapshot.getValue(Room.class);
                        mRoomsMap.remove(dataSnapshot.getKey());
                        listener.onUpdateReady(mRoomsMap);
                        Log.d(TAG, "onChildRemoved: " + dataSnapshot.getKey() + ", " + room);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildMoved");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mRoomsMap.clear();
                        listener.onUpdateReady(mRoomsMap);
                        Log.d(TAG, "onCancelled");
                    }
                });
    }
}