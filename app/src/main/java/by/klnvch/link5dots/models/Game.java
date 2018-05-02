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

import android.graphics.Point;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.utils.DotsArrayUtils;
import by.klnvch.link5dots.utils.RandomGenerator;

public class Game {

    private static final int N = 20;
    private static final int M = 20;

    private final Dot[][] net = new Dot[N][M];
    private final List<Dot> dots = new ArrayList<>();

    private final int mHostDotType;
    private final int mGuestDotType;
    private HighScore mScore = null;
    private transient ArrayList<Dot> mWinningLine = null;

    private Game(@DotType int hostDotType) {
        mHostDotType = hostDotType;
        mGuestDotType = hostDotType == Dot.HOST ? Dot.GUEST : Dot.HOST;

        for (int i = 0; i < N; i++)
            for (int j = 0; j < M; j++)
                net[i][j] = new Dot(i, j);
    }

    private Game() {
        mHostDotType = Dot.HOST;
        mGuestDotType = Dot.GUEST;

        for (int i = 0; i < N; i++)
            for (int j = 0; j < M; j++)
                net[i][j] = new Dot(i, j);
    }

    /***
     * Creates updated version of game to display.
     * Sets dots on the paper and checks for a winning line.
     *
     * @param dots dots sent from service or null if empty
     * @param hostDotType dot type of device owner
     * @return new object of the game
     */
    @NonNull
    public static Game createGame(@Nullable List<Dot> dots, @DotType int hostDotType) {
        // create an empty game
        Game game = new Game(hostDotType);
        if (dots != null) {
            // set dots
            game.dots.addAll(dots);
            for (Dot dot : dots) {
                game.net[dot.getX()][dot.getY()] = dot;
            }
            // check for the end of the game
            game.isOver();
        }

        return game;
    }

    @NonNull
    public static Game generateGame(@Nullable Long seed) {
        Game game = new Game();

        if (seed != null) {
            List<Point> points = RandomGenerator.generateUniqueSixDots(seed);
            for (int i = 0; i != points.size(); ++i) {
                Point p = points.get(i);
                int x = 8 + p.x;
                int y = 8 + p.y;

                game.net[x][y].setType(i % 2 == 0 ? Dot.HOST : Dot.GUEST);
                game.net[x][y].setId(i);
                game.net[x][y].setTimestamp(System.currentTimeMillis() / 1000);
                game.dots.add(game.net[x][y]);
            }
        }
        return game;
    }

    private void prepareScore() {
        final Dot firstDot = getFirstDot();
        final Dot lastDot = getLastDot();

        if (mWinningLine != null && firstDot != null && lastDot != null) {
            final long time = (System.currentTimeMillis() - firstDot.getTimestamp()) / 1000;
            final int movesDone = lastDot.getId() + 1;

            if (mWinningLine.get(0).getType() == mHostDotType) {
                mScore = new HighScore(movesDone, time, HighScore.WON);
            } else {
                mScore = new HighScore(movesDone, time, HighScore.LOST);
            }
        }
    }

    public boolean checkCorrectness(int x, int y) {
        return isInBound(x, y) && net[x][y].getType() == Dot.EMPTY && mWinningLine == null;
    }

    private boolean isInBound(int x, int y) {
        return x >= 0 && y >= 0 && x < N && y < M;
    }

    /**
     * Checks if the last dot appears in a winning row
     *
     * @return null or array of winning dots
     */
    @Nullable
    public ArrayList<Dot> isOver() {
        if (mWinningLine == null) {
            mWinningLine = DotsArrayUtils.findWinningLine(dots);

            if (mWinningLine != null) {
                prepareScore();
            }
        }
        return mWinningLine;
    }

    @Nullable
    public HighScore getCurrentScore() {
        if (mWinningLine != null && mScore == null) {
            prepareScore();
        }
        return mScore;
    }

    @Nullable
    private Dot getFirstDot() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (net[i][j].getType() != Dot.EMPTY && net[i][j].getId() == 0) {
                    return net[i][j];
                }
            }
        }
        return null;
    }

    @Nullable
    public Dot getLastDot() {
        Dot result = null;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (net[i][j].getType() != Dot.EMPTY) {
                    if (result != null) {
                        if (result.getId() < net[i][j].getId()) {
                            result = net[i][j];
                        }
                    } else {
                        result = net[i][j];
                    }
                }
            }
        }
        return result;
    }

    public boolean isHostDot(int i, int j) {
        return net[i][j].getType() == mHostDotType;
    }

    public boolean isGuestDot(int i, int j) {
        return net[i][j].getType() == mGuestDotType;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                result.append(net[i][j]).append(" ");
            }
            result.append("\n");
        }
        return result.toString();
    }

    @IntDef({Dot.HOST, Dot.GUEST})
    private @interface DotType {
    }
}