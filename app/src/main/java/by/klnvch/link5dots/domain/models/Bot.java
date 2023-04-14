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

package by.klnvch.link5dots.domain.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class Bot {

    //temporary table for dots rate
    private final int[][] net1;
    private final int[][] net2;
    private final Board board;

    //masks
    private static final int[][] masks = new int[][]{
            // 0 - empty, 1 - my, 2 - not my, 3 - any
            //my wins - 100%
            new int[]{1, 1, 1, 1, 1, 3, 3, 3, 3},
            new int[]{3, 1, 1, 1, 1, 1, 3, 3, 3},
            new int[]{3, 3, 1, 1, 1, 1, 1, 3, 3},
            new int[]{3, 3, 3, 1, 1, 1, 1, 1, 3},
            new int[]{3, 3, 3, 3, 1, 1, 1, 1, 1},
            //my is near win - 99%
            new int[]{0, 1, 1, 1, 1, 0, 3, 3, 3},
            new int[]{3, 0, 1, 1, 1, 1, 0, 3, 3},
            new int[]{3, 3, 0, 1, 1, 1, 1, 0, 3},
            new int[]{3, 3, 3, 0, 1, 1, 1, 1, 0},
            //my can't win - 0%
            new int[]{2, 3, 3, 3, 1, 2, 3, 3, 3},
            new int[]{3, 2, 3, 3, 1, 3, 2, 3, 3},
            new int[]{3, 3, 2, 3, 1, 3, 3, 2, 3},
            new int[]{3, 3, 3, 2, 1, 3, 3, 3, 2},
            //my is beaten - 30%
            new int[]{2, 1, 1, 1, 1, 0, 3, 3, 3},
            new int[]{3, 2, 1, 1, 1, 1, 0, 3, 3},
            new int[]{3, 3, 2, 1, 1, 1, 1, 0, 3},
            new int[]{3, 3, 3, 2, 1, 1, 1, 1, 0},

            new int[]{3, 3, 3, 0, 1, 1, 1, 1, 2},
            new int[]{3, 3, 0, 1, 1, 1, 1, 2, 3},
            new int[]{3, 0, 1, 1, 1, 1, 2, 3, 3},
            new int[]{0, 1, 1, 1, 1, 2, 3, 3, 3},

            new int[]{1, 1, 1, 0, 1, 0, 0, 3, 3},
            new int[]{3, 3, 0, 0, 1, 0, 1, 1, 1},
            //my is beaten - 15%
            new int[]{3, 2, 1, 1, 1, 0, 0, 3, 3},
            new int[]{2, 1, 1, 0, 1, 0, 3, 3, 3},
            new int[]{2, 1, 0, 1, 1, 0, 3, 3, 3},
            new int[]{1, 1, 0, 0, 1, 3, 3, 3, 3},
            new int[]{1, 0, 1, 0, 1, 3, 3, 3, 3},
            new int[]{1, 0, 0, 1, 1, 3, 3, 3, 3},

            new int[]{3, 3, 0, 0, 1, 1, 1, 2, 3},
            new int[]{3, 3, 3, 0, 1, 0, 1, 1, 2},
            new int[]{3, 3, 3, 0, 1, 1, 0, 1, 2},
            new int[]{3, 3, 3, 3, 1, 0, 0, 1, 1},
            new int[]{3, 3, 3, 3, 1, 0, 1, 0, 1},
            new int[]{3, 3, 3, 3, 1, 1, 0, 0, 1}
    };

    @Inject
    public Bot(@NonNull Board board) {
        this.board = board;
        net1 = new int[board.getWidth()][board.getHeight()];
        net2 = new int[board.getWidth()][board.getHeight()];
    }

    @NonNull
    public Dot findAnswer(@NonNull List<Dot> dots) {
        final Dot[][] net = new Dot[board.getWidth()][board.getHeight()];
        for (int i = 0; i < board.getWidth(); i++)
            for (int j = 0; j < board.getHeight(); j++)
                net[i][j] = new Dot(i, j, Dot.EMPTY, 0);

        for (Dot dot : dots) {
            net[dot.getX()][dot.getY()] = dot;
        }

        float maxUserRate = -1;
        ArrayList<Dot> listUser = new ArrayList<>();
        float maxBotRate = -1;
        ArrayList<Dot> listBot = new ArrayList<>();

        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                if (net[i][j].getType() == Dot.EMPTY) {
                    float userRate = getDotRate(net, net[i][j], Dot.HOST);
                    net1[i][j] = (int) userRate;
                    float botRate = getDotRate(net, net[i][j], Dot.GUEST);
                    net2[i][j] = (int) botRate;

                    //user rates
                    if (userRate == maxUserRate) {
                        listUser.add(net[i][j]);
                    } else if (userRate > maxUserRate) {
                        maxUserRate = userRate;
                        listUser.clear();
                        listUser.add(net[i][j]);
                    }

                    //bot rates
                    if (botRate == maxBotRate) {
                        listBot.add(net[i][j]);
                    } else if (botRate > maxBotRate) {
                        maxBotRate = botRate;
                        listBot.clear();
                        listBot.add(net[i][j]);
                    }

                } else {
                    net1[i][j] = net2[i][j] = -1;
                }
            }
        }

        float max = -1;
        ArrayList<Dot> resultList = new ArrayList<>();

        if (maxBotRate >= maxUserRate) {
            for (Dot dot : listBot) {
                if (max == net1[dot.getX()][dot.getY()]) {
                    resultList.add(dot);
                } else if (max < net1[dot.getX()][dot.getY()]) {
                    max = net1[dot.getX()][dot.getY()];
                    resultList.clear();
                    resultList.add(dot);
                }
            }
        } else {
            for (Dot dot : listUser) {
                if (max == net2[dot.getX()][dot.getY()]) {
                    resultList.add(dot);
                } else if (max < net2[dot.getX()][dot.getY()]) {
                    max = net2[dot.getX()][dot.getY()];
                    resultList.clear();
                    resultList.add(dot);
                }
            }

        }

        Dot result = null;
        max = -1;

        //density checking
        for (Dot off : resultList) {
            int temp = getDensity(net, off.getX(), off.getY());
            if (temp > max) {
                max = temp;
                result = off;
            }
        }

        if (result == null) {
            throw new RuntimeException("Bot error");
        }

        return result;
    }

    private float getDotRate(Dot[][] net, Dot dot, int type) {
        int[] array2 = new int[4];
        //
        array2[0] = (int) getDotRateInLine(net, dot, type, 1, 0);
        array2[1] = (int) getDotRateInLine(net, dot, type, 1, 1);
        array2[2] = (int) getDotRateInLine(net, dot, type, 0, 1);
        array2[3] = (int) getDotRateInLine(net, dot, type, -1, 1);

        //it is only one dot or building a line is not impossible
        if (array2[0] <= 10 && array2[1] <= 10 && array2[2] <= 10 && array2[3] <= 10) {
            return 0;
        }

        //bingo bot has win
        if (array2[0] >= 100 || array2[1] >= 100 || array2[2] >= 100 || array2[3] >= 100) {
            return 100000000;
        }

        //bingo bot has win
        //if(array2[0]>=99 || array2[1]>=99 || array2[2]>=99 || array2[3]>=99){
        //	return 9999;
        //}

        for (int i = 0; i < 4; i++) {
            array2[i] *= array2[i];
        }

        return array2[0] + array2[1] + array2[2] + array2[3];
    }

    private int getDensity(Dot[][] net, int x, int y) {
        int result = 0;

        for (int i = -4; i != 5; ++i) {
            for (int j = -4; j != 5; ++j) {
                if (board.isInside(new Point(x + i, y + j)) && net[x + i][y + j].getType() == Dot.HOST) {
                    if (i > 3 || i < -3 || j > 3 || j < -3) {
                        result += 1;
                    } else if (i > 2 || i < -2 || j > 2 || j < -2) {
                        result += 3;
                    } else if (i > 1 || i < -1 || j > 1 || j < -1) {
                        result += 9;
                    } else {
                        result += 27;
                    }
                }
            }
        }

        return result;
    }

    private float getDotRateInLine(Dot[][] net, Dot dot, int type, int dx, int dy) {
        int x = dot.getX();
        int y = dot.getY();

        int[] array = new int[9];

        //init the 9-length array with types of dots
        for (int i = -4; i != 5; ++i) {
            if (board.isInside(new Point(x + dx * i, y + dy * i))) {
                array[i + 4] = net[x + dx * i][y + dy * i].getType();
            } else {
                array[i + 4] = 0;
            }
        }
        array[4] = type;

        //check array in masks
        float result = checkMasks(array, type);
        if (result != -1.0f) {
            return result;
        }

        //check 5 variants
        // 0-not exist; type-is the same; EMPTY-is empty
        //in the 5-length array can be only the same type and EMPTY
        int max = 0;

        for (int i = 0; i != 5; ++i) {
            int count = 0;
            for (int j = 0; j != 5; ++j) {
                if (array[i + j] == type) {//the same type, increase count
                    ++count;
                } else if (array[i + j] == Dot.EMPTY) {//empty, do not increase

                } else {//there is something bad
                    count = 0;
                    break;
                }
            }
            if (count > max) max = count;
        }

        if (max >= 5) return 100.0f;
        else if (max == 4) return 60.0f;
        else if (max == 3) return 50.0f;
        else if (max == 2) return 30.0f;
        else if (max == 1) return 10.0f;
        else return 0.0f;
    }

    /*
     * check line in masks array and if it matches return line rate
     * in the other case return -1.0f
     */
    private float checkMasks(int[] line, int type) {
        for (int i = 0; i != 5; ++i) {
            if (checkMasks(line, masks[i], type)) return 100.0f;
        }

        for (int i = 5; i != 9; ++i) {
            if (checkMasks(line, masks[i], type)) return 99.0f;
        }

        for (int i = 13; i != 23; ++i) {
            if (checkMasks(line, masks[i], type)) return 40.0f;
        }

        for (int i = 23; i != 34; ++i) {
            if (checkMasks(line, masks[i], type)) return 20.0f;
        }

        for (int i = 9; i != 14; ++i) {
            if (checkMasks(line, masks[i], type)) return 0.0f;
        }

        return -1.0f;
    }

    /*
     * compare one line to one masks
     *
     * if line satisfy to mask return true
     */
    private boolean checkMasks(int[] line, int[] mask, int type) {
        //type - my dots number
        for (int i = 0; i != 9; ++i) {
            switch (mask[i]) {
                case 0:
                    if (line[i] != Dot.EMPTY) return false;
                    break;
                case 1:
                    if (line[i] != type) return false;
                    break;
                case 2:
                    if (line[i] == Dot.EMPTY || line[i] == type) return false;
                    break;
                case 3:
                    break;

            }
        }
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                result.append(net1[i][j]).append(" ");
            }
            result.append("\n");
        }
        result.append("\n");
        for (int i = 0; i < board.getWidth(); i++) {
            for (int j = 0; j < board.getHeight(); j++) {
                result.append(net2[i][j]).append(" ");
            }
            result.append("\n");
        }
        return result.toString();
    }
}
