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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

@Entity(tableName = "rooms")
public class Room {

    public static final int STATE_CREATED = 0;
    public static final int STATE_DELETED = 1;
    public static final int STATE_STARTED = 2;

    private static final String TIME_TEMPLATE = "MMM-dd HH:mm";
    private static final String TIME_FORMAT = "%d:%02d";

    @PrimaryKey
    @NonNull
    private String key;
    @ColumnInfo(name = "state")
    private int state;
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    @ColumnInfo(name = "dots")
    private ArrayList<Dot> dots;
    @Embedded(prefix = "user_1_")
    private User user1;
    @Embedded(prefix = "user_2_")
    private User user2;

    public Room() {
    }

    @NonNull
    public static Room newRoom(@NonNull User user) {
        final Room room = new Room();
        room.key = generateKey();
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
        return new Gson().fromJson(json, Room.class);
    }

    @NonNull
    private static String generateKey() {
        return Long.toHexString(System.currentTimeMillis())
                + '_' + Long.toHexString(new Random().nextLong());
    }

    public void addDot(@NonNull Dot dot) {
        if (dots != null) {
            dot.setTimestamp(System.currentTimeMillis());
            dots.add(dot);
        } else {
            dots = new ArrayList<>(Collections.singletonList(dot));
        }
    }

    public void newGame() {
        if (dots != null) {
            dots.clear();
            key = generateKey();
        }
    }

    @NonNull
    public User getAnotherUser(@NonNull User user) {
        if (user1.equals(user))
            return user2;
        else
            return user1;
    }

    @NonNull
    public String getStartTime() {
        final Format timeFormat = new SimpleDateFormat(TIME_TEMPLATE, Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }

    @Nullable
    public String getDuration() {
        if (dots != null && !dots.isEmpty()) {
            final Dot dot = dots.get(dots.size() - 1);
            final long duration = (dot.getTimestamp() - timestamp) / 1000;
            final int min = (int) (duration / 60);
            final int sec = (int) (duration % 60);
            return String.format(Locale.getDefault(), TIME_FORMAT, min, sec);
        }
        return null;
    }

    @Override
    public String toString() {
        return getStartTime() + '\n' + user1.getName();
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

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Dot> getDots() {
        return dots;
    }

    public void setDots(ArrayList<Dot> dots) {
        this.dots = dots;
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
}