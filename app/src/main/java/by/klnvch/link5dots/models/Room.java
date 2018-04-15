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

package by.klnvch.link5dots.models;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class Room {

    public static final int STATE_CREATED = 0;
    public static final int STATE_DELETED = 1;
    public static final int STATE_STARTED = 2;

    private static final String TIME_TEMPLATE = "MMM-dd HH:mm";

    private String key;
    private int state;
    private long timestamp;
    private List<Dot> dots;
    private User user1;
    private User user2;

    private Room() {

    }

    @NonNull
    public static Room newRoom(@NonNull User user) {
        final Room room = new Room();
        room.key = null;
        room.timestamp = System.currentTimeMillis();
        room.state = STATE_CREATED;
        room.dots = new ArrayList<>();
        room.user1 = user;

        return room;
    }

    @NonNull
    public static Room newRoom(@NonNull String key, @NonNull User user) {
        final Room room = new Room();
        room.key = key;
        room.timestamp = System.currentTimeMillis();
        room.state = STATE_CREATED;
        room.dots = new ArrayList<>();
        room.user1 = user;

        return room;
    }

    @NonNull
    public static Room fromJson(@NonNull String json) {
        Log.d(json, "str: " + json);
        return new Gson().fromJson(json, Room.class);
    }

    public void addDot(@NonNull Dot dot) {
        if (dots != null) {
            dots.add(dot);
        } else {
            dots = Collections.singletonList(dot);
        }
    }

    public void newGame() {
        if (dots != null) {
            dots.clear();
        }
    }

    public User getAnotherUser(@NonNull User user) {
        if (user1.equals(user))
            return user2;
        else
            return user1;
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
        final Format timeFormat = new SimpleDateFormat("MMM-dd HH:mm", Locale.getDefault());
        final String time = timeFormat.format(new Date(timestamp));
        final String username = user1.getName();
        return username + '\n' + time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Room) {
            return ((Room) obj).key.equals(this.key);
        }
        return false;
    }

    @NonNull
    public String toJson() {
        return new Gson().toJson(this);
    }
}