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

import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

public class GameViewState {

    public boolean isFocusVisible = true;
    /**
     * Displacement of the left top corner of the bitmap from the left top corner of the screen
     */
    public PointF basePoint = new PointF();
    /**
     * Scale to apply to the base point and arr1 after scaling
     */
    public float minScale = 0.0f;
    public float maxScale = 3.0f;
    public float scale = 1.0f;
    /**
     * Used for displaying background
     * <p>
     * onSizeChanged				translate
     * onScale						scale
     * correctMatrixAndBasePoint	translate
     * onScroll					translate
     */
    public transient Matrix matrix = new Matrix();
    private float[] matrixArray = new float[9];

    @NonNull
    public static GameViewState fromJson(@Nullable String json) {
        if (json != null) {
            GameViewState result = new Gson().fromJson(json, GameViewState.class);
            result.matrix.setValues(result.matrixArray);
            return result;
        } else {
            return new GameViewState();
        }
    }

    @NonNull
    public String toJson() {
        matrix.getValues(matrixArray);
        return new Gson().toJson(this);
    }
}