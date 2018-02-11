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

import android.support.annotation.NonNull;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import by.klnvch.link5dots.BuildConfig;
import by.klnvch.link5dots.models.Dot;

@SuppressWarnings("unused")
public class Room {

    static final String CHILD_ROOM = BuildConfig.DEBUG ? "rooms_debug" : "rooms";
    static final String CHILD_STATE = "state";

    static final int STATE_CREATED = 0;
    static final int STATE_DELETED = 1;
    static final int STATE_STARTED = 2;

    private static final Format TIME_FORMAT =
            new SimpleDateFormat("MMM-dd HH:mm", Locale.getDefault());

    private String key;
    private int state;
    private long timestamp;
    private List<Dot> dots;
    private User user1;
    private User user2;

    private Room() {

    }

    public static Room newRoom(@NonNull String key, @NonNull User user) {
        Room room = new Room();
        room.key = key;
        room.timestamp = System.currentTimeMillis();
        room.state = STATE_CREATED;
        room.dots = new ArrayList<>();
        room.user1 = user;

        room.dots.add(new Dot(1, 2, 2, 0));
        room.dots.add(new Dot(2, 3, 4, 1));
        room.dots.add(new Dot(3, 4, 2, 2));
        room.dots.add(new Dot(4, 5, 4, 3));

        return room;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public List<Dot> getDots() {
        return dots;
    }

    public void setDots(List<Dot> dots) {
        this.dots = dots;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        String time = TIME_FORMAT.format(new Date(timestamp));
        String username = user1.getName();
        return time + ": " + username;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Room && ((Room) obj).key.equals(this.key);
    }
}