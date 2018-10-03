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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.HighScore;
import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.models.User;

import static com.google.common.base.Preconditions.checkNotNull;

public class RoomUtils {

    private static final String TIME_TEMPLATE = "MMM-dd HH:mm";

    /**
     * Creates game with AI
     *
     * @param user1 user
     * @param user2 bot
     * @return game with AI
     */
    @NonNull
    public static Room createBotGame(@NonNull User user1, @NonNull User user2) {
        checkNotNull(user1);
        checkNotNull(user2);

        final Room room = new Room();
        room.setKey(MathUtils.generateKey());
        room.setState(Room.STATE_CREATED);
        room.setTimestamp(System.currentTimeMillis());
        room.setDots(new ArrayList<>());
        room.setUser1(user1);
        room.setUser2(user2);
        room.setType(Room.TYPE_BOT);
        room.setSend(false);
        room.setTest(false);
        return room;
    }

    /**
     * Creates game for two players on the same device
     *
     * @return game for two players on the same device
     */
    @NonNull
    public static Room createTwoGame() {
        final Room room = new Room();
        room.setKey(MathUtils.generateKey());
        room.setState(Room.STATE_CREATED);
        room.setTimestamp(System.currentTimeMillis());
        room.setDots(new ArrayList<>());
        room.setUser1(null);
        room.setUser2(null);
        room.setType(Room.TYPE_TWO_PLAYERS);
        room.setSend(false);
        room.setTest(false);
        return room;
    }

    @NonNull
    public static Room createMultiplayerGame(@NonNull User user, int type) {
        checkNotNull(user);

        final Room room = new Room();
        room.setKey(MathUtils.generateKey());
        room.setState(Room.STATE_CREATED);
        room.setTimestamp(System.currentTimeMillis());
        room.setDots(new ArrayList<>());
        room.setUser1(user);
        room.setUser2(null);
        room.setType(type);
        room.setSend(false);
        room.setTest(false);
        return room;
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
        room.setDots(new ArrayList<>());
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

    private static long getDuration(@NonNull Room room) {
        checkNotNull(room);
        if (!isEmpty(room)) {
            final Dot lastDot = DotsArrayUtils.getLastDot(room.getDots());
            return (lastDot.getTimestamp() - room.getTimestamp());
        }
        return 0;
    }

    public static long getDurationInSeconds(@NonNull Room room) {
        return getDuration(room) / 1000;
    }

    public static String formatStartTime(@NonNull Room room) {
        checkNotNull(room);

        final Format timeFormat = new SimpleDateFormat(TIME_TEMPLATE, Locale.getDefault());
        return timeFormat.format(new Date(room.getTimestamp()));
    }

    @NonNull
    public static User getAnotherUser(@NonNull Room room, @NonNull User user) {
        checkNotNull(room);
        checkNotNull(user);

        if (room.getUser1().equals(user))
            return room.getUser2();
        else
            return room.getUser1();
    }

    @Nullable
    private static Integer getLastDotType(@NonNull Room room) {
        checkNotNull(room);

        if (!isEmpty(room)) {
            return DotsArrayUtils.getLastDot(room.getDots()).getType();
        }
        return null;
    }

    public static int getHostDotType(@NonNull Room room, @NonNull User host) {
        checkNotNull(room);

        return host.equals(room.getUser1()) ? Dot.HOST : Dot.GUEST;
    }

    @NonNull
    public static HighScore getHighScore(@NonNull Room room, @Nullable User user) {
        checkNotNull(room);

        final Dot lastDot = DotsArrayUtils.getLastDot(room.getDots());

        final long time = (lastDot.getTimestamp() - room.getTimestamp()) / 1000;
        final int movesDone = lastDot.getId() + 1;

        if (user != null) {
            if (lastDot.getType() == getHostDotType(room, user)) {
                return new HighScore(movesDone, time, HighScore.WON);
            } else {
                return new HighScore(movesDone, time, HighScore.LOST);
            }
        } else {
            return new HighScore(movesDone, time, -1);
        }
    }

    @NonNull
    public static Room addDotWithBotAnswer(@NonNull Room room, @NonNull Dot dot) {
        checkNotNull(room);
        checkNotNull(dot);

        addDot(room, dot, Dot.HOST);
        if (DotsArrayUtils.findWinningLine(room.getDots()) == null) {
            addDot(room, Bot.findAnswer(room.getDots()), Dot.GUEST);
        }

        return room;
    }

    @NonNull
    public static Room addDotAsAnotherType(@NonNull Room room, @NonNull Dot dot) {
        checkNotNull(room);
        checkNotNull(dot);

        final Integer lastDotType = getLastDotType(room);
        if (lastDotType == null || lastDotType != Dot.HOST)
            addDot(room, dot, Dot.HOST);
        else
            addDot(room, dot, Dot.GUEST);

        return room;
    }

    @NonNull
    public static Room addDotMultiplayer(@NonNull Room room, @NonNull User user, @NonNull Dot dot) {
        checkNotNull(room);
        checkNotNull(user);
        checkNotNull(dot);

        final Integer lastDotType = getLastDotType(room);
        final int hostDotType = getHostDotType(room, user);

        if (lastDotType == null || lastDotType != hostDotType) {
            addDot(room, dot, hostDotType);
        }

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