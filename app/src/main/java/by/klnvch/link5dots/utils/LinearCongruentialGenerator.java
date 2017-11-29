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

import java.util.ArrayList;
import java.util.List;

public class LinearCongruentialGenerator {

    public static final int MAX_SEED = 65536;

    private static final int a = 15538;
    private static final int c = 1;
    private static final int m = 65536;

    private static int getNext(int x) {
        return (a * x + c) % m;
    }

    @NonNull
    public static List<Point> generateUniqueSixDots(int seed) {
        ArrayList<Point> points = new ArrayList<>(6);

        while (points.size() != 6) {
            seed = getNext(seed);
            int x = seed % 5;

            seed = getNext(seed);
            int y = seed % 5;

            Point point = new Point(x, y);

            if (!points.contains(point)) {
                points.add(point);
            }
        }

        return points;
    }
}