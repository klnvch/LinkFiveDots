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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.utils.LinearCongruentialGenerator;

public class Game {

    private static final int N = 20;
    private static final int M = 20;

    public final Dot[][] net = new Dot[N][M];
    private HighScore currentScore = null;
    private transient ArrayList<Dot> winningLine = null;
    private int movesDone = 0;
    private long startTime;

    private Game() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                net[i][j] = new Dot(i, j);
            }
        }
    }

    @NonNull
    public static Game generateGame(@Nullable Integer seed) {
        Game game = new Game();

        if (seed != null) {
            game.startTime = System.currentTimeMillis() / 1000;
            game.movesDone = 3;

            List<Point> points = LinearCongruentialGenerator.generateUniqueSixDots(seed);
            for (int i = 0; i != points.size(); ++i) {
                Point p = points.get(i);
                int x = 8 + p.x;
                int y = 8 + p.y;
                if (i % 2 == 0) {
                    game.net[x][y].changeStatus(Dot.USER, i);
                } else {
                    game.net[x][y].changeStatus(Dot.OPPONENT, i);
                }
            }
        }

        return game;
    }

    @NonNull
    public static Game fromJson(@Nullable String json) {
        if (json != null) {
            Game result = new Gson().fromJson(json, Game.class);
            result.winningLine = result.isOver();
            return result;
        } else {
            return new Game();
        }
    }

    @NonNull
    public String toJson() {
        return new Gson().toJson(this);
    }

    private void prepareScore() {
        long time = System.currentTimeMillis() / 1000 - startTime;

        if (winningLine != null) {

            if (winningLine.get(0).getType() == Dot.USER) {
                currentScore = new HighScore(movesDone, time, HighScore.WON);
            } else {
                currentScore = new HighScore(getNumberOfMoves(), time, HighScore.LOST);
            }

        }
    }

    public void setDot(int x, int y, int type) {

        Dot theLastDot = getLastDot();
        if (!checkCorrectness(x, y) || (theLastDot != null && theLastDot.getType() == type)) {
            return;
        }

        if (getNumberOfMoves() == 0) {//it is the first move, start stop watch
            startTime = System.currentTimeMillis() / 1000;
        }
        if (type == Dot.USER) {
            movesDone++;
        }
        net[x][y].changeStatus(type, getNumberOfMoves());
        winningLine = isOver();

        if (winningLine != null) {
            prepareScore();
        }
    }

    private int getNumberOfMoves() {
        Dot dot = getLastDot();
        if (dot != null) {
            return dot.getIndex() + 1;
        }
        return 0;
    }

    public boolean checkCorrectness(int x, int y) {
        return isInBound(x, y) && net[x][y].getType() == Dot.EMPTY && winningLine == null;
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
     * Checks if five dots line has been built
     *
     * @return null or array of five dots
     */
    public ArrayList<Dot> isOver() {

        if (winningLine == null) {
            if (getNumberOfMoves() < 5) return null;

            Dot lastDot = getLastDot();

            if (lastDot == null) return null;

            ArrayList<Dot> result;

            result = getDotsNumber(lastDot, 1, 0);
            if (result.size() >= 5) {
                winningLine = result;
                return result;
            }

            result = getDotsNumber(lastDot, 1, 1);
            if (result.size() >= 5) {
                winningLine = result;
                return result;
            }

            result = getDotsNumber(lastDot, 0, 1);
            if (result.size() >= 5) {
                winningLine = result;
                return result;
            }

            result = getDotsNumber(lastDot, -1, 1);
            if (result.size() >= 5) {
                winningLine = result;
                return result;
            }

            return null;
        } else {
            return winningLine;
        }
    }

    public void undo(int moves) {
        if (moves > 0) {    // if moves == 1, game with two human
            Dot d = getLastDot();
            if (d != null) {
                d.changeStatus(Dot.EMPTY, -1);
            }
        }
        if (moves > 1) {    // if moves == 2, game with bot
            Dot d = getLastDot();
            if (d != null && d.getType() == Dot.USER) {
                d.changeStatus(Dot.EMPTY, -1);
            }
        }

        winningLine = null;
        winningLine = isOver();
    }

    public HighScore getCurrentScore() {
        return currentScore;
    }

    @Nullable
    public Dot getLastDot() {
        Dot result = null;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                if (net[i][j].getType() != Dot.EMPTY) {
                    if (result != null) {
                        if (result.getIndex() < net[i][j].getIndex()) {
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
                copyNet[i][j] = this.net[i][j].copy();
            }
        }
        return copyNet;
    }
}