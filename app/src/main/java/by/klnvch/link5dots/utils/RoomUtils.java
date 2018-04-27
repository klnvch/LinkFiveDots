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

package by.klnvch.link5dots.utils;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;

import static com.google.common.base.Preconditions.checkNotNull;

public class RoomUtils {

    @NonNull
    public static Room createBotGame(@NonNull User user1, @NonNull User user2) {
        checkNotNull(user1);
        checkNotNull(user2);

        final Room room = Room.newRoom(user1, Room.TYPE_BOT);
        room.setUser2(user2);
        return room;
    }

    @NonNull
    public static Room createTwoGame() {
        return Room.newRoom(Room.TYPE_TWO_PLAYERS);
    }

    @NonNull
    public static Room createOnlineGame(@NonNull String key, @NonNull User user) {
        checkNotNull(key);
        checkNotNull(user);

        final Room room = new Room();
        room.setKey(key);
        room.setTimestamp(System.currentTimeMillis());
        room.setState(Room.STATE_CREATED);
        room.setUser1(user);
        room.setType(Room.TYPE_ONLINE);
        return room;
    }

    public static boolean isEmpty(@NonNull Room room) {
        checkNotNull(room);

        final ArrayList<Dot> dots = room.getDots();
        return dots == null || dots.isEmpty();
    }

    @NonNull
    public static Room undo(@NonNull Room room) {
        checkNotNull(room);

        if (!isEmpty(room)) {
            final ArrayList<Dot> dots = room.getDots();
            dots.remove(dots.size() - 1);
            room.setDots(dots);
        }

        return room;
    }

    @NonNull
    public static Room newGame(@NonNull Room room, @Nullable Long seed) {
        checkNotNull(room);

        room.setKey(MathUtils.generateKey());
        room.setTimestamp(System.currentTimeMillis());
        room.setDots(null);
        room.setSend(false);

        if (seed != null) {
            final List<Point> points = RandomGenerator.generateUniqueSixDots(seed);

            for (int i = 0; i != points.size(); ++i) {
                final Point p = points.get(i);
                final int x = 8 + p.x;
                final int y = 8 + p.y;
                final Dot dot = new Dot(x, y);

                addDot(room, dot, i % 2 == 0 ? Dot.HOST : Dot.GUEST);
            }
        }

        return room;
    }

    @Nullable
    private static Integer getLastDotType(@NonNull Room room) {
        checkNotNull(room);

        if (!isEmpty(room)) {
            final ArrayList<Dot> dots = room.getDots();
            return dots.get(dots.size() - 1).getType();
        }
        return null;
    }

    public static int getHostDotType(@NonNull Room room, @NonNull User host) {
        checkNotNull(room);
        checkNotNull(room);

        return host.equals(room.getUser1()) ? Dot.HOST : Dot.GUEST;
    }

    @NonNull
    public static Room addDotWithBotAnswer(@NonNull Room room, @NonNull Dot dot) {
        checkNotNull(room);
        checkNotNull(room.getDots());
        checkNotNull(dot);

        addDot(room, dot, Dot.HOST);
        addDot(room, Bot.findAnswer(room.getDots()), Dot.GUEST);

        return room;
    }

    @NonNull
    public static Room addDotAsAnotherType(@NonNull Room room, @NonNull Dot dot) {
        checkNotNull(room);
        checkNotNull(dot);

        final Integer lastDotType = RoomUtils.getLastDotType(room);
        if (lastDotType == null || lastDotType != Dot.HOST)
            RoomUtils.addDot(room, dot, Dot.HOST);
        else
            RoomUtils.addDot(room, dot, Dot.GUEST);


        return room;
    }

    private static void addDot(@NonNull Room room, @NonNull Dot dot, int type) {
        checkNotNull(room);
        checkNotNull(dot);
        // get array of dots
        ArrayList<Dot> dots = room.getDots();
        if (dots == null) dots = new ArrayList<>();
        // update dot
        dot.setTimestamp(System.currentTimeMillis());
        dot.setId(dots.size());
        dot.setType(type);
        // add dot to the end
        dots.add(dot);
        // save array of dots
        room.setDots(dots);
        room.setSend(false);
    }
}