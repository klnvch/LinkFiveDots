/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.domain.models.BotGameScore;
import by.klnvch.link5dots.domain.models.Dot;
import by.klnvch.link5dots.domain.models.GameResult;
import by.klnvch.link5dots.domain.models.IRoom;
import by.klnvch.link5dots.domain.models.InitialGameGenerator;
import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.domain.models.NetworkUser;
import by.klnvch.link5dots.domain.models.Point;
import by.klnvch.link5dots.domain.models.RoomExt;
import by.klnvch.link5dots.domain.models.RoomKeyGenerator;
import by.klnvch.link5dots.domain.models.RoomState;

public class RoomUtils {

    @NonNull
    public static NetworkRoom createMultiplayerGame(@NonNull NetworkUser user, int type) {
        final RoomKeyGenerator roomKeyGenerator = new RoomKeyGenerator();
        return new NetworkRoom(
                roomKeyGenerator.get(),
                System.currentTimeMillis(),
                new ArrayList<>(),
                user,
                null,
                type,
                RoomState.CREATED
        );
    }

    @NonNull
    public static NetworkRoom newGame(@NonNull NetworkRoom room, @Nullable Long seed) {
        final ArrayList<Dot> dots = new ArrayList<>();

        if (seed != null) {
            final InitialGameGenerator initialGameGenerator = new InitialGameGenerator();
            final List<Point> points = initialGameGenerator.get(seed);

            for (int i = 0; i != points.size(); ++i) {
                final Point p = points.get(i);
                final int x = 8 + p.getX();
                final int y = 8 + p.getY();
                final Point dot = new Point(x, y);

                dots.add(new Dot(p.getX(), p.getY(), i % 2 == 0 ? Dot.HOST : Dot.GUEST, 0));
            }
        }

        final RoomKeyGenerator roomKeyGenerator = new RoomKeyGenerator();
        return new NetworkRoom(
                roomKeyGenerator.get(),
                System.currentTimeMillis(),
                dots,
                room.getUser1(),
                room.getUser2(),
                room.getType(),
                room.getState()
        );
    }

    @NonNull
    public static NetworkUser getAnotherUser(@NonNull NetworkRoom room, @NonNull NetworkUser user) {
        checkNotNull(room);
        checkNotNull(user);

        if (user.equals(room.getUser1()))
            return room.getUser2();
        else
            return room.getUser1();
    }

    @Nullable
    private static Integer getLastDotType(@NonNull IRoom room) {
        final Dot lastDot = room.getLatsDot();
        return lastDot != null ? lastDot.getType() : null;
    }

    @Deprecated
    @NonNull
    public static BotGameScore getHighScore(@NonNull NetworkRoom room, @Nullable NetworkUser user) {
        final Dot lastDot = room.getLatsDot();

        final int time = room.getDuration();
        final int movesDone = room.getDots().size();

        if (user != null && lastDot != null) {
            final GameResult gameResult;
            if (lastDot.getType() == RoomExt.getHostDotType(room, user)) {
                gameResult = GameResult.WON;
            } else {
                gameResult = GameResult.LOST;
            }
            return new BotGameScore(movesDone, time, room.getEndTime(), gameResult);
        } else {
            return new BotGameScore(movesDone, time, room.getEndTime(), GameResult.LOST);
        }
    }

    @NonNull
    public static NetworkRoom addDotMultiplayer(@NonNull NetworkRoom room, @NonNull NetworkUser user, @NonNull Point p) {
        final Integer lastDotType = getLastDotType(room);
        final int hostDotType = RoomExt.getHostDotType(room, user);

        final ArrayList<Dot> dots = new ArrayList<>(room.getDots());
        if (lastDotType == null || lastDotType != hostDotType) {
            final int dt = (int) (System.currentTimeMillis() - room.getTimestamp());
            dots.add(new Dot(p.getX(), p.getY(), hostDotType, dt));
        }

        return new NetworkRoom(room.getKey(), room.getTimestamp(), dots, room.getUser1(), room.getUser2(), room.getType(), room.getState());
    }
}