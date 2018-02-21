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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class BitmapCreator {

    @NonNull
    public static Bitmap createDot(@ColorInt int color, float density) {
        final int n = (int) Math.ceil(density * Circle.N);
        final int[] colors = new int[n * n];

        for (int i = 0; i != n; ++i) {
            for (int j = 0; j != n; ++j) {
                if (Circle.isInside(i, j, density)) {
                    colors[i * n + j] = color;
                } else {
                    colors[i * n + j] = Color.TRANSPARENT;
                }
            }
        }
        return Bitmap.createBitmap(colors, n, n, Bitmap.Config.ARGB_8888);
    }

    @NonNull
    public static Bitmap createRing(@ColorInt int color, float density) {
        final int n = (int) Math.ceil(density * Ring.N);
        final int[] colors = new int[n * n];

        for (int i = 0; i != n; ++i) {
            for (int j = 0; j != n; ++j) {
                if (Ring.isInside(i, j, density)) {
                    colors[i * n + j] = color;
                } else {
                    colors[i * n + j] = Color.TRANSPARENT;
                }
            }
        }
        return Bitmap.createBitmap(colors, n, n, Bitmap.Config.ARGB_8888);
    }

    @NonNull
    public static Bitmap createCross(@ColorInt int color, float density) {
        final int n = (int) Math.ceil(density * Cross.N);
        final int[] colors = new int[n * n];

        for (int i = 0; i != n; ++i) {
            for (int j = 0; j != n; ++j) {
                if (Cross.isInside(i, j, density)) {
                    colors[i * n + j] = color;
                } else {
                    colors[i * n + j] = Color.TRANSPARENT;
                }
            }
        }
        return Bitmap.createBitmap(colors, n, n, Bitmap.Config.ARGB_8888);
    }

    private static class Circle {
        private static final int N = 16;
        private static final double R = N / 2.0;

        private static boolean isInside(int x, int y, float density) {
            final double r = R * density;
            return (x - r) * (x - r) + (y - r) * (y - r) <= r * r;
        }
    }

    private static class Ring {
        private static final int N = 16;
        private static final double R1 = N / 2.0;
        private static final double R2 = N / 4.0;

        private static boolean isInside(int x, int y, float density) {
            final double r1 = R1 * density;
            final double r2 = R2 * density;

            final double d = (x - r1) * (x - r1) + (y - r1) * (y - r1);
            return d <= r1 * r1 && d >= r2 * r2;
        }
    }

    private static class Cross {
        private static final int N = 16;
        private static final double R = N / 6.0;

        private static boolean isInside(int x, int y, float density) {
            final double n = N * density;
            final double r = R * density;
            return x + r >= y && x - r <= y || n - x + r >= y && n - x - r <= y;
        }
    }
}