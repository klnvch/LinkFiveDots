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

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.utils.LinearCongruentialGenerator;

public class Game {

    private static final int N = 20;
    private static final int M = 20;

    private final Dot[][] net = new Dot[N][M];

    private final int mHostDotType;
    private final int mGuestDotType;
    private HighScore mScore = null;
    private transient ArrayList<Dot> mWinningLine = null;
    private int movesDone = 0;

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
            for (Dot dot : dots) {
                game.net[dot.getX()][dot.getY()] = dot;
            }
            // check for the end of the game
            game.isOver();
        }

        return game;
    }

    @NonNull
    public static Game generateGame(@Nullable Integer seed) {
        Game game = new Game();

        if (seed != null) {
            game.movesDone = 3;

            List<Point> points = LinearCongruentialGenerator.generateUniqueSixDots(seed);
            for (int i = 0; i != points.size(); ++i) {
                Point p = points.get(i);
                int x = 8 + p.x;
                int y = 8 + p.y;

                game.net[x][y].setType(i % 2 == 0 ? Dot.HOST : Dot.GUEST);
                game.net[x][y].setId(i);
                game.net[x][y].setTimestamp(System.currentTimeMillis() / 1000);
            }
        }

        return game;
    }

    @NonNull
    public static Game fromJson(@Nullable String json) {
        if (json != null) {
            Game game = new Gson().fromJson(json, Game.class);
            game.isOver();
            return game;
        } else {
            return new Game();
        }
    }

    @NonNull
    public String toJson() {
        return new Gson().toJson(this);
    }

    private void prepareScore() {
        final Dot firstDot = getFirstDot();

        if (mWinningLine != null && firstDot != null) {
            final long time = System.currentTimeMillis() / 1000 - firstDot.getTimestamp();

            if (mWinningLine.get(0).getType() == Dot.HOST) {
                mScore = new HighScore(movesDone, time, HighScore.WON);
            } else {
                mScore = new HighScore(getNumberOfMoves(), time, HighScore.LOST);
            }
        }
    }

    @Nullable
    public Dot setHostDot(@NonNull Dot dot) {
        dot.setType(mHostDotType);
        return setDot(dot);
    }

    @Nullable
    public Dot setGuestDot(@NonNull Dot dot) {
        dot.setType(mGuestDotType);
        return setDot(dot);
    }

    @Nullable
    private Dot setDot(@NonNull Dot dot) {
        Dot theLastDot = getLastDot();
        if (!checkCorrectness(dot.getX(), dot.getY()) ||
                (theLastDot != null && theLastDot.getType() == dot.getType())) {
            return null;
        }

        if (dot.getType() == mHostDotType) {
            movesDone++;
        }

        int x = dot.getX();
        int y = dot.getY();
        net[x][y].setType(dot.getType());
        net[x][y].setId(getNumberOfMoves());
        net[x][y].setTimestamp(System.currentTimeMillis() / 1000);

        isOver();

        return net[x][y];
    }

    private int getNumberOfMoves() {
        Dot dot = getLastDot();
        if (dot != null) {
            return dot.getId() + 1;
        }
        return 0;
    }

    public boolean checkCorrectness(int x, int y) {
        return isInBound(x, y) && net[x][y].getType() == Dot.EMPTY && mWinningLine == null;
    }

    private boolean isInBound(int x, int y) {
        return x >= 0 && y >= 0 && x < N && y < M;
    }

    private ArrayList<Dot> getDotsNumber(Dot dot, int dx, int dy) {

        int x = dot.getX();
        int y = dot.getY();

        ArrayList<Dot> result = new ArrayList<>();
        result.add(dot);

        for (int k = 1; (k < 5) && isInBound(x + dx * k, y + dy * k) && net[x + dx * k][y + dy * k].getType() == dot.getType(); k++) {
            result.add(net[x + dx * k][y + dy * k]);
        }
        for (int k = 1; (k < 5) && isInBound(x - dx * k, y - dy * k) && net[x - dx * k][y - dy * k].getType() == dot.getType(); k++) {
            result.add(0, net[x - dx * k][y - dy * k]);
        }

        return result;
    }

    /**
     * Checks if the last dot appears in a winning row
     *
     * @return null or array of winning dots
     */
    @Nullable
    public ArrayList<Dot> isOver() {
        if (mWinningLine == null) {
            final Dot lastDot = getLastDot();

            if (lastDot == null) return null;

            final int[][] directions = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};

            for (int[] d : directions) {
                ArrayList<Dot> result = getDotsNumber(lastDot, d[0], d[1]);
                if (result.size() >= 5) {
                    mWinningLine = result;
                    break;
                }
            }

            if (mWinningLine != null) {
                prepareScore();
            }
        }
        return mWinningLine;
    }

    public void undo(int moves) {
        if (moves > 0) {    // if moves == 1, game with two human
            Dot d = getLastDot();
            if (d != null) {
                d.setType(Dot.EMPTY);
                d.setId(-1);
            }
        }
        if (moves > 1) {    // if moves == 2, game with bot
            Dot d = getLastDot();
            if (d != null && d.getType() == Dot.HOST) {
                d.setType(Dot.EMPTY);
                d.setId(-1);
            }
        }

        mScore = null;
        mWinningLine = null;
    }

    @Nullable
    public HighScore getCurrentScore() {
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

    @NonNull
    public Dot[][] getCopyOfNet() {
        Dot[][] copyNet = new Dot[this.net.length][];
        for (int i = 0; i != this.net.length; ++i) {
            copyNet[i] = new Dot[this.net[i].length];
            for (int j = 0; j != this.net[i].length; ++j) {
                copyNet[i][j] = new Dot(this.net[i][j]);
            }
        }
        return copyNet;
    }

    @IntDef({Dot.HOST, Dot.GUEST})
    private @interface DotType {
    }
}