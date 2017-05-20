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
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

public class GameViewState {

    private static final String TAG = "GameViewState";

    private static final float MIN_SCALE = 0.2f;
    private static final float MAX_SCALE = 3.0f;
    private final transient Matrix matrix = new Matrix();
    private final float[] matrixArray = new float[9];
    public boolean isFocusVisible = true;

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

    private float getScale() {
        matrix.getValues(matrixArray);
        return matrixArray[Matrix.MSCALE_X];
    }

    public void translate(float dx, float dy) {
        matrix.postTranslate(dx, dy);
    }

    public void scale(float s, float px, float py) {
        matrix.postScale(s, s, px, py);
    }

    public void copyMatrix(@NonNull Matrix srcMatrix) {
        srcMatrix.set(matrix);
    }

    @NonNull
    public Matrix getMatrix() {
        return matrix;
    }

    @NonNull
    public PointF invertMapPoint(float x, float y) {
        float[] result = new float[]{x, y};
        Matrix inverseCopy = new Matrix();
        if (matrix.invert(inverseCopy)) {
            inverseCopy.mapPoints(result);
        } else {
            Log.d(TAG, "matrix inversion error");
        }
        return new PointF(result[0], result[1]);
    }

    public void correctParameters(float screenWidth, float screenHeight, float paperSize) {
        //
        // validate scale: minScale <= scale <= maxScale
        //
        if (getScale() < MIN_SCALE) {
            float s = MIN_SCALE / getScale();
            scale(s, screenWidth / 2.0f, screenHeight / 2.0f);
            Log.d(TAG, "scale up to min");
        }
        if (getScale() > MAX_SCALE) {
            float s = MAX_SCALE / getScale();
            scale(s, screenWidth / 2.0f, screenHeight / 2.0f);
            Log.d(TAG, "scale down to max");
        }

        RectF paperRect = new RectF(0, 0, paperSize, paperSize);
        matrix.mapRect(paperRect);
        //
        // validate translate: if paper is less than screen, than center it
        //
        float dx = 0;
        float dy = 0;
        if (paperRect.width() < screenWidth) {
            dx = screenWidth / 2.0f - paperRect.centerX();
            Log.d(TAG, "translate to center width");
        } else {
            if (paperRect.left > 0) {
                dx = -paperRect.left;
                Log.d(TAG, "translate to the left corner");
            }
            if (paperRect.right < screenWidth) {
                dx = screenWidth - paperRect.right;
                Log.d(TAG, "translate to the right corner");
            }
        }
        if (paperRect.height() < screenHeight) {
            dy = screenHeight / 2.0f - paperRect.centerY();
            Log.d(TAG, "translate to the center height");
        } else {
            if (paperRect.top > 0) {
                dy = -paperRect.top;
                Log.d(TAG, "translate to the top corner");
            }
            if (paperRect.bottom < screenHeight) {
                dy = screenHeight - paperRect.bottom;
                Log.d(TAG, "translate to the bottom corner");
            }
        }
        translate(dx, dy);
    }

    @NonNull
    public String toJson() {
        matrix.getValues(matrixArray);
        return new Gson().toJson(this);
    }
}