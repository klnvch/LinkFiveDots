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

import android.support.annotation.NonNull;

import java.util.Random;

public class MathUtils {

    /**
     * Finds index of closest element from an array to the specified value
     *
     * @param array an array
     * @param value value
     * @return index from an the array
     */
    public static int findClosestIndex(float[] array, float value) {
        int result = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i != array.length; ++i) {
            final float dist = Math.abs(value - array[i]);
            if (min > dist) {
                min = dist;
                result = i;
            }
        }
        return result;
    }

    @NonNull
    public static String generateKey() {
        return Long.toHexString(System.currentTimeMillis())
                + '_' + Long.toHexString(new Random().nextLong());
    }
}