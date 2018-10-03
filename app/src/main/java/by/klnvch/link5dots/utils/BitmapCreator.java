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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

public class BitmapCreator {

    public static final int DOT = 1;
    public static final int RING = 2;
    public static final int CROSS = 3;
    public static final int LINE_H = 4;
    public static final int LINE_V = 5;
    public static final int LINE_D_L = 6;
    public static final int LINE_D_R = 7;

    private static final int SIZE_1 = 16;
    private static final int SIZE_2 = 32;

    @NonNull
    public static Bitmap createBitmap(@BitmapType int type, @ColorInt int color, float density) {
        final Shape shape;
        switch (type) {
            case DOT:
                shape = new Circle(density);
                break;
            case RING:
                shape = new Ring(density);
                break;
            case CROSS:
                shape = new Cross(density);
                break;
            case LINE_H:
                shape = new HorizontalLine(density);
                break;
            case LINE_V:
                shape = new VerticalLine(density);
                break;
            case LINE_D_L:
                shape = new LeftDiagonalLine(density);
                break;
            case LINE_D_R:
                shape = new RightDiagonalLine(density);
                break;
            default:
                throw new RuntimeException("unknown parameter");
        }
        final int n = shape.getSize();
        final int[] colors = new int[n * n];

        for (int i = 0; i != n; ++i) {
            for (int j = 0; j != n; ++j) {
                if (shape.isInside(i, j)) {
                    colors[j * n + i] = color;
                } else {
                    colors[j * n + i] = Color.TRANSPARENT;
                }
            }
        }
        return Bitmap.createBitmap(colors, n, n, Bitmap.Config.ARGB_8888);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DOT, RING, CROSS, LINE_H, LINE_V, LINE_D_L, LINE_D_R})
    private @interface BitmapType {
    }

    private static abstract class Shape {
        final int size;
        final float density;

        Shape(float density, int size) {
            this.density = density;
            this.size = size;
        }

        abstract boolean isInside(int x, int y);

        int getSize() {
            return (int) Math.ceil(density * size);
        }
    }

    private static class Circle extends Shape {
        final double R;

        Circle(float density) {
            super(density, SIZE_1);
            R = size / 2.0;
        }

        @Override
        boolean isInside(int x, int y) {
            final double r = R * density;
            return (x - r) * (x - r) + (y - r) * (y - r) <= r * r;
        }
    }

    private static class Ring extends Shape {
        final double R1;
        final double R2;

        Ring(float density) {
            super(density, SIZE_1);
            R1 = size / 2.0;
            R2 = size / 4.0;
        }

        @Override
        boolean isInside(int x, int y) {
            final double r1 = R1 * density;
            final double r2 = R2 * density;

            final double d = (x - r1) * (x - r1) + (y - r1) * (y - r1);
            return d <= r1 * r1 && d >= r2 * r2;
        }
    }

    private static class Cross extends Shape {
        final double R;

        Cross(float density) {
            super(density, SIZE_1);
            R = size / 6.0;
        }

        @Override
        boolean isInside(int x, int y) {
            final double n = size * density;
            final double r = R * density;
            return x + r >= y && x - r <= y || n - x + r >= y && n - x - r <= y;
        }
    }

    private static abstract class Line extends Shape {
        final double width;

        Line(float density) {
            super(density, SIZE_2);
            width = size / 6.0;
        }
    }

    private static class HorizontalLine extends Line {
        HorizontalLine(float density) {
            super(density);
        }

        @Override
        boolean isInside(int x, int y) {
            final double n = size * density;
            final double r = width * density;
            return y >= (n - r) / 2.0 && y <= (n + r) / 2.0;
        }
    }

    private static class VerticalLine extends Line {
        VerticalLine(float density) {
            super(density);
        }

        @Override
        boolean isInside(int x, int y) {
            final double n = size * density;
            final double r = width * density;
            return x >= (n - r) / 2.0 && x <= (n + r) / 2.0;
        }
    }

    private static class LeftDiagonalLine extends Line {
        LeftDiagonalLine(float density) {
            super(density);
        }

        @Override
        boolean isInside(int x, int y) {
            final double r = width * density;
            return x + r >= y && x - r <= y;
        }
    }

    private static class RightDiagonalLine extends Line {
        RightDiagonalLine(float density) {
            super(density);
        }

        @Override
        boolean isInside(int x, int y) {
            final double n = size * density;
            final double r = width * density;
            return n - x + r >= y && n - x - r <= y;
        }
    }
}