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

package by.klnvch.link5dots;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

import java.util.ArrayList;

import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.models.HighScore;

public class GameView extends View {

    private static final String TAG = "GameView";
    /**
     * number of lines vertical or horizontal
     */
    private final static int GRID_SIZE = 20;
    /**
     * The background bitmap is paper size pixels wide. This array contains positions of lines of number GRID_SIZE
     */
    public float screenWidth;
    public float screenHeight;
    private float[] arr1;

    private Bitmap background;          // paper
    private Bitmap userDot;             // red dot
    private Bitmap botDot;              // blue dot
    private Bitmap userHorLine;
    private Bitmap userVerLine;
    private Bitmap userDiagonal1Line;
    private Bitmap userDiagonal2Line;
    private Bitmap botHorLine;
    private Bitmap botVerLine;
    private Bitmap botDiagonal1Line;
    private Bitmap botDiagonal2Line;
    private Bitmap arrows;

    private float dotSize;
    private float arrowsSize;
    private float paperSize;
    private float lineLength;
    private float lineThickness;

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private Game mGameState = new Game();
    private GameViewState mViewState = new GameViewState();
    private OnMoveDoneListener onMoveDoneListener;
    private OnGameEndListener onGameEndListener;
    private Bitmap scaledBotDot;
    private Bitmap scaledUserDot;
    private Bitmap newline;
    private Bitmap scaledArrow;

    public GameView(Context context) {
        super(context);
        initGameView(context);
    }

    public GameView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
        initGameView(context);
    }

    public GameView(Context context, AttributeSet attrSet, int defStyle) {
        super(context, attrSet, defStyle);
        initGameView(context);
    }

    private void initGameView(Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);

        // set gesture detectors
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        // load bitmaps. why do we need matrix?
        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        userDot = BitmapFactory.decodeResource(getResources(), R.drawable.red_dot);
        botDot = BitmapFactory.decodeResource(getResources(), R.drawable.blue_dot);
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(90.0f);
        userHorLine = BitmapFactory.decodeResource(getResources(), R.drawable.redlinehor);
        userVerLine = Bitmap.createBitmap(userHorLine, 0, 0, userHorLine.getWidth(), userHorLine.getHeight(), rotateMatrix, true);
        userDiagonal1Line = BitmapFactory.decodeResource(getResources(), R.drawable.redlinediag1);
        userDiagonal2Line = Bitmap.createBitmap(userDiagonal1Line, 0, 0, userDiagonal1Line.getWidth(), userDiagonal1Line.getHeight(), rotateMatrix, true);
        botHorLine = BitmapFactory.decodeResource(getResources(), R.drawable.bluelinehor);
        botVerLine = Bitmap.createBitmap(botHorLine, 0, 0, botHorLine.getWidth(), botHorLine.getHeight(), rotateMatrix, true);
        botDiagonal1Line = BitmapFactory.decodeResource(getResources(), R.drawable.bluelinediag1);
        botDiagonal2Line = Bitmap.createBitmap(botDiagonal1Line, 0, 0, botDiagonal1Line.getWidth(), botDiagonal1Line.getHeight(), rotateMatrix, true);

        arrows = BitmapFactory.decodeResource(getResources(), R.drawable.arrows);

        // set bitmap sizes
        dotSize = userDot.getWidth();
        arrowsSize = arrows.getWidth();
        paperSize = background.getWidth();
        lineLength = userHorLine.getWidth();
        lineThickness = userHorLine.getHeight();
        //
        arr1 = new float[GRID_SIZE];
        for (int i = 0; i != GRID_SIZE; ++i) {
            arr1[i] = paperSize / (2 * GRID_SIZE) + (i * paperSize) / GRID_SIZE;
        }
    }

    public void setOnMoveDoneListener(@NonNull OnMoveDoneListener listener) {
        this.onMoveDoneListener = listener;
    }

    public void setOnGameEndListener(@NonNull OnGameEndListener listener) {
        this.onGameEndListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        //set base point if it is not set yet
        if (screenWidth == 0 || screenHeight == 0) {//there is nothing to restore. it is the first run
            //set width and height
            screenWidth = w;
            screenHeight = h;

            float dx = (w - paperSize) / 2.0f;
            float dy = (h - paperSize) / 2.0f;
            mViewState.basePoint.set(dx, dy);
            mViewState.matrix.postTranslate(dx, dy);

            onScale(mViewState.scale, w / 2.0f, h / 2.0f);
        } else {
            //set base point and matrix if screen sizes have changed
            float dx = (w - screenWidth) / 2.0f;
            float dy = (h - screenHeight) / 2.0f;
            mViewState.basePoint.x += dx;
            mViewState.basePoint.y += dy;
            mViewState.matrix.postTranslate(dx, dy);

            //set new width and height
            screenWidth = w;
            screenHeight = h;

            onScale(1.0f, w / 2.0f, h / 2.0f);
        }
        super.onSizeChanged(w, h, oldW, oldH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw background
        canvas.drawBitmap(background, mViewState.matrix, null);

        // Draw dots
        for (int i = 0; i != GRID_SIZE; ++i) {
            for (int j = 0; j != GRID_SIZE; ++j) {
                float dX = mViewState.basePoint.x + arr1[i] * mViewState.scale - dotSize * mViewState.scale / 2.0f;
                float dY = mViewState.basePoint.y + arr1[j] * mViewState.scale - dotSize * mViewState.scale / 2.0f;
                if (mGameState.net[i][j].getType() == Dot.USER) {
                    canvas.drawBitmap(scaledUserDot, dX, dY, null);
                }
                if (mGameState.net[i][j].getType() == Dot.OPPONENT) {
                    canvas.drawBitmap(scaledBotDot, dX, dY, null);
                }
            }
        }
        // Draw dots winning line
        ArrayList<Dot> winningLine = mGameState.isOver();
        if (winningLine != null) {
            Dot firstDot = winningLine.get(0);
            Dot lastDot = winningLine.get(winningLine.size() - 1);

            float x1 = mViewState.basePoint.x + arr1[firstDot.getX()] * mViewState.scale;
            float y1 = mViewState.basePoint.y + arr1[firstDot.getY()] * mViewState.scale;
            float x2 = mViewState.basePoint.x + arr1[lastDot.getX()] * mViewState.scale;
            float y2 = mViewState.basePoint.y + arr1[lastDot.getY()] * mViewState.scale;

            float shiftX = 0.0f;
            float shiftY = 0.0f;

            if (y1 == y2) {//horizontal line
                shiftY = lineThickness * mViewState.scale / 2.0f;
            } else if (x1 == x2) {//vertical line
                shiftX = lineThickness * mViewState.scale / 2.0f;
            } else if (x1 > x2) {//diagonal left to right
                shiftX = lineLength * mViewState.scale;
            }// else if (x2 > x1) {//diagonal right to left
            //}

            for (int i = 0; i != winningLine.size() - 1; ++i) {
                float dX = mViewState.basePoint.x + arr1[winningLine.get(i).getX()] * mViewState.scale - shiftX;
                float dY = mViewState.basePoint.y + arr1[winningLine.get(i).getY()] * mViewState.scale - shiftY;
                canvas.drawBitmap(newline, dX, dY, null);
            }
        }

        // Draw four arrows
        Dot lastDot = mGameState.getLastDot();
        if (lastDot != null && mViewState.isFocusVisible) {
            float dX = mViewState.basePoint.x + arr1[lastDot.getX()] * mViewState.scale - (arrowsSize * mViewState.scale) / 2.0f;
            float dY = mViewState.basePoint.y + arr1[lastDot.getY()] * mViewState.scale - (arrowsSize * mViewState.scale) / 2.0f;
            canvas.drawBitmap(scaledArrow, dX, dY, null);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        performClick();
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void switchHideArrow() {
        mViewState.isFocusVisible = !mViewState.isFocusVisible;
        invalidate();
    }

    public void undoLastMove(int moves) {
        mGameState.undo(moves);
        invalidate();
    }

    public void resetGame() {
        mGameState.reset();
        invalidate();
    }

    public Dot[][] getCopyOfNet() {
        Dot[][] copyNet = new Dot[mGameState.net.length][];
        for (int i = 0; i != mGameState.net.length; ++i) {
            copyNet[i] = new Dot[mGameState.net[i].length];
            for (int j = 0; j != mGameState.net[i].length; ++j) {
                copyNet[i][j] = mGameState.net[i][j].copy();
            }
        }
        return copyNet;
    }

    /**
     * Finds nearest horizontal line from 20 possible lines
     *
     * @param y - position on the screen
     * @return line number
     */
    private int findTapedHorLine(float y) {
        int result = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i != GRID_SIZE; ++i) {
            float dist = Math.abs(y - mViewState.basePoint.y - arr1[i] * mViewState.scale);
            if (min > dist) {
                min = dist;
                result = i;
            }
        }
        return result;
    }

    private int findTapedVerLine(float x) {
        int result = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i != GRID_SIZE; ++i) {
            float dist = Math.abs(x - mViewState.basePoint.x - arr1[i] * mViewState.scale);
            if (min > dist) {
                min = dist;
                result = i;
            }
        }
        return result;
    }

    public void setDot(Dot dot) {
        mGameState.setDot(dot.getX(), dot.getY(), dot.getType());
        invalidate();
        isOver();
    }

    public void isOver() {
        if (mGameState.isOver() != null) {
            updateLinesBitmaps();
            HighScore highScore = mGameState.getCurrentScore();
            if (highScore != null) {
                if (onGameEndListener != null) {
                    onGameEndListener.onGameEnd(highScore);
                } else {
                    Log.e(TAG, "listener is null");
                }
            }
        }
    }

    public HighScore getHighScore() {
        return mGameState.getCurrentScore();
    }

    private void onScale(float scaleFactor, float px, float py) {

        float newScale = mViewState.scale * scaleFactor;

        //in case new scale < minimal possible scale < current scale, increase new scale
        //check it in case if the image is less than screen
        //it is necessary to align image to the screen borders in any case and minimize as much as possible
        if (paperSize * newScale < screenHeight && paperSize * newScale < screenWidth) {
            float minPosScale = Math.min(screenHeight / paperSize, screenWidth / paperSize);
            if (minPosScale > newScale) {
                newScale = minPosScale;
                scaleFactor = newScale / mViewState.scale;
            }
            if (minPosScale > mViewState.maxScale) {
                mViewState.maxScale = minPosScale;
            }
        }

        //check if it is allowed to scale
        if ((newScale < mViewState.minScale && scaleFactor < 1.0f) || (newScale > mViewState.maxScale && scaleFactor > 1.0f))
            return;

        if (paperSize * newScale >= screenHeight || paperSize * newScale >= screenWidth || scaleFactor > 1.0f) {

            //scale game_board ground
            mViewState.matrix.postScale(scaleFactor, scaleFactor, px, py);
            updateScale(newScale);
            mViewState.basePoint.x += (1 - scaleFactor) * (px - mViewState.basePoint.x);
            mViewState.basePoint.y += (1 - scaleFactor) * (py - mViewState.basePoint.y);

            //move it
            //center x if the bitmap width is less than the screen width
            correctMatrixAndBasePoint();

            invalidate();
        }
    }

    private void updateScale(float newScale) {
        mViewState.scale = newScale;

        scaledBotDot = Bitmap.createScaledBitmap(botDot, (int) (dotSize * mViewState.scale), (int) (dotSize * mViewState.scale), true);
        scaledUserDot = Bitmap.createScaledBitmap(userDot, (int) (dotSize * mViewState.scale), (int) (dotSize * mViewState.scale), true);
        updateLinesBitmaps();
        scaledArrow = Bitmap.createScaledBitmap(arrows, (int) (arrowsSize * mViewState.scale), (int) (arrowsSize * mViewState.scale), true);
    }

    private void updateLinesBitmaps() {
        ArrayList<Dot> winningLine = mGameState.isOver();
        if (winningLine != null) {
            Dot firstDot = winningLine.get(0);
            Dot lastDot = winningLine.get(winningLine.size() - 1);

            float x1 = mViewState.basePoint.x + arr1[firstDot.getX()] * mViewState.scale;
            float y1 = mViewState.basePoint.y + arr1[firstDot.getY()] * mViewState.scale;
            float x2 = mViewState.basePoint.x + arr1[lastDot.getX()] * mViewState.scale;
            float y2 = mViewState.basePoint.y + arr1[lastDot.getY()] * mViewState.scale;

            if (y1 == y2) {//horizontal line
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userHorLine, (int) (lineLength * mViewState.scale), (int) (lineThickness * mViewState.scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botHorLine, (int) (lineLength * mViewState.scale), (int) (lineThickness * mViewState.scale), true);
                }
            } else if (x1 == x2) {//vertical line
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userVerLine, (int) (lineThickness * mViewState.scale), (int) (lineLength * mViewState.scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botVerLine, (int) (lineThickness * mViewState.scale), (int) (lineLength * mViewState.scale), true);
                }
            } else if (x1 > x2) {//diagonal left to right
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userDiagonal1Line, (int) (lineLength * mViewState.scale), (int) (lineLength * mViewState.scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botDiagonal1Line, (int) (lineLength * mViewState.scale), (int) (lineLength * mViewState.scale), true);
                }
            } else if (x2 > x1) {//diagonal right to left
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userDiagonal2Line, (int) (lineLength * mViewState.scale), (int) (lineLength * mViewState.scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botDiagonal2Line, (int) (lineLength * mViewState.scale), (int) (lineLength * mViewState.scale), true);
                }
            }
        }
    }

    private void correctMatrixAndBasePoint() {
        if (paperSize * mViewState.scale <= screenWidth) {
            float newBaseX = (screenWidth - paperSize * mViewState.scale) / 2.0f;
            mViewState.matrix.postTranslate(newBaseX - mViewState.basePoint.x, 0);
            mViewState.basePoint.x = newBaseX;
        }
        //move left if bitmap is too right
        else if (mViewState.basePoint.x > 0.0f) {
            mViewState.matrix.postTranslate(-mViewState.basePoint.x, 0);
            mViewState.basePoint.x = 0.0f;
        }
        //move right if bitmap is too left
        else if (screenWidth > mViewState.basePoint.x + paperSize * mViewState.scale) {
            float dx = screenWidth - (mViewState.basePoint.x + paperSize * mViewState.scale);
            mViewState.matrix.postTranslate(dx, 0);
            mViewState.basePoint.x += dx;
        }
        //center y if the bitmap height is less than the screen height
        if (paperSize * mViewState.scale <= screenHeight) {
            float newBaseY = (screenHeight - paperSize * mViewState.scale) / 2.0f;
            mViewState.matrix.postTranslate(0, newBaseY - mViewState.basePoint.y);
            mViewState.basePoint.y = newBaseY;
        }
        //move up if bitmap is too low
        else if (mViewState.basePoint.y > 0.0f) {
            mViewState.matrix.postTranslate(0, -mViewState.basePoint.y);
            mViewState.basePoint.y = 0.0f;
        }
        //move down if bitmap is too high
        else if (screenHeight > mViewState.basePoint.y + paperSize * mViewState.scale) {
            float dy = screenHeight - (mViewState.basePoint.y + paperSize * mViewState.scale);
            mViewState.matrix.postTranslate(0, dy);
            mViewState.basePoint.y += dy;
        }
    }

    @NonNull
    public Game getGameState() {
        return this.mGameState;
    }

    public void setGameState(@NonNull Game game) {
        this.mGameState = game;
        isOver();
        invalidate();
    }

    @NonNull
    public GameViewState getViewState() {
        return this.mViewState;
    }

    public void setViewState(@NonNull GameViewState viewState) {
        this.mViewState = viewState;
        //updateScale(this.mViewState.scale);
        onSizeChanged((int) screenWidth, (int) screenHeight, (int) screenWidth, (int) screenHeight);
        invalidate();
    }

    public interface OnMoveDoneListener {
        void onMoveDone(@NonNull Dot currentDot, @Nullable Dot previousDot);
    }

    public interface OnGameEndListener {
        void onGameEnd(@NonNull HighScore highScore);
    }

    private class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {

            float newBaseX = mViewState.basePoint.x - dx;
            float newBaseY = mViewState.basePoint.y - dy;
            //check if the new position of the bitmap is in the screen
            if (newBaseX <= 0.0f && newBaseX + paperSize * mViewState.scale >= screenWidth) {
                mViewState.basePoint.x = newBaseX;
                mViewState.matrix.postTranslate(-dx, 0);
            }
            if (newBaseY <= 0.0f && newBaseY + paperSize * mViewState.scale >= screenHeight) {
                mViewState.basePoint.y = newBaseY;
                mViewState.matrix.postTranslate(0, -dy);
            }
            invalidate();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mGameState.isOver() == null) {
                int xl = findTapedVerLine(e.getX());
                int yl = findTapedHorLine(e.getY());
                boolean res = mGameState.checkCorrectness(xl, yl);
                if (!res) {
                    return true;
                }

                invalidate();

                if (onMoveDoneListener != null) {
                    onMoveDoneListener.onMoveDone(new Dot(xl, yl), mGameState.getLastDot());
                } else {
                    Log.e(TAG, "listener is null");
                }

                //
                if (mGameState.isOver() != null) {
                    updateLinesBitmaps();
                    HighScore highScore = mGameState.getCurrentScore();

                    if (onGameEndListener != null) {
                        onGameEndListener.onGameEnd(highScore);
                    } else {
                        Log.e(TAG, "listener is null");
                    }
                }
            }
            return true;
        }
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.isInProgress()) {
                GameView.this.onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            }
            return true;
        }
    }
}