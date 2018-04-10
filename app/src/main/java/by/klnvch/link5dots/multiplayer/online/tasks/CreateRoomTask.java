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

package by.klnvch.link5dots.multiplayer.online.tasks;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.multiplayer.common.interfaces.OnTargetCreatedListener;
import by.klnvch.link5dots.multiplayer.targets.TargetOnline;

public class CreateRoomTask {
    public static void createRoom(@NonNull DatabaseReference database,
                                  @NonNull OnTargetCreatedListener listener,
                                  @NonNull Room room) {

        final String key = database.child(Room.CHILD_ROOM).push().getKey();
        room.setKey(key);
        database
                .child(Room.CHILD_ROOM)
                .child(key)
                .setValue(room)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onTargetCreated(new TargetOnline(room));
                    } else {
                        final Exception exception = task.getException();
                        if (exception != null) {
                            listener.onTargetCreationFailed(exception);
                        } else {
                            listener.onTargetCreationFailed(new NullPointerException());
                        }
                    }
                });
    }
}