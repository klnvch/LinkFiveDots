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
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import by.klnvch.link5dots.models.Dot;
import by.klnvch.link5dots.models.Game;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.utils.BitmapCreator;
import by.klnvch.link5dots.utils.MathUtils;

public class GameView extends View {

    private static final String TAG = "GameView";
    /**
     * number of lines vertical or horizontal
     */
    private final static int GRID_SIZE = 20;
    private final float[] mLineLocations = new float[GRID_SIZE];
    private final float[] mDotLocations = new float[GRID_SIZE];
    private final float[] mArrowLocations = new float[GRID_SIZE];
    private final Matrix mDrawMatrix = new Matrix();
    /**
     * The background bitmap is paper size pixels wide. This array contains positions of lines of number GRID_SIZE
     */
    private float mScreenWidth;
    private float mScreenHeight;
    private Bitmap mBitmapPaper;
    private Bitmap mBitmapUserDot;             // red dot or cross
    private Bitmap mBitmapBotDot;              // blue dot or ring
    private Bitmap mBitmapArrows;
    private Bitmap userHorLine;
    private Bitmap userVerLine;
    private Bitmap userDiagonal1Line;
    private Bitmap userDiagonal2Line;
    private Bitmap botHorLine;
    private Bitmap botVerLine;
    private Bitmap botDiagonal1Line;
    private Bitmap botDiagonal2Line;
    private float mPaperSize;
    private float mLineSize;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Game mGameState = Game.generateGame(null);
    private GameViewState mViewState = new GameViewState();
    private OnMoveDoneListener mOnMoveDoneListener;
    private OnGameEndListener mOnGameEndListener;
    private Bitmap mWinningLine = null;
    private float mWinningLineDX = 0;
    private float mWinningLineDY = 0;

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(@NonNull Context context, @SettingsUtils.DotsType int dotsType) {
        Log.d(TAG, "initGameView");

        setFocusable(true);
        setFocusableInTouchMode(true);

        // set gesture detectors
        mGestureDetector = new GestureDetector(context, new GestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        // load bitmaps. why do we need matrix?
        final float density = getResources().getDisplayMetrics().density;
        final int colorRed = ContextCompat.getColor(context, R.color.dot_color_red);
        final int colorBlue = ContextCompat.getColor(context, R.color.dot_color_blue);
        mBitmapPaper = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        if (dotsType == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            mBitmapUserDot = BitmapCreator.createBitmap(BitmapCreator.DOT, colorRed, density);
            mBitmapBotDot = BitmapCreator.createBitmap(BitmapCreator.DOT, colorBlue, density);
        } else {
            mBitmapUserDot = BitmapCreator.createBitmap(BitmapCreator.CROSS, colorRed, density);
            mBitmapBotDot = BitmapCreator.createBitmap(BitmapCreator.RING, colorBlue, density);
        }

        userHorLine = BitmapCreator.createBitmap(BitmapCreator.LINE_H, colorRed, density);
        userVerLine = BitmapCreator.createBitmap(BitmapCreator.LINE_V, colorRed, density);
        userDiagonal2Line = BitmapCreator.createBitmap(BitmapCreator.LINE_D_L, colorRed, density);
        userDiagonal1Line = BitmapCreator.createBitmap(BitmapCreator.LINE_D_R, colorRed, density);

        botHorLine = BitmapCreator.createBitmap(BitmapCreator.LINE_H, colorBlue, density);
        botVerLine = BitmapCreator.createBitmap(BitmapCreator.LINE_V, colorBlue, density);
        botDiagonal2Line = BitmapCreator.createBitmap(BitmapCreator.LINE_D_L, colorBlue, density);
        botDiagonal1Line = BitmapCreator.createBitmap(BitmapCreator.LINE_D_R, colorBlue, density);

        mBitmapArrows = BitmapFactory.decodeResource(getResources(), R.drawable.arrows);

        // set bitmap sizes
        final float dotSize = mBitmapUserDot.getWidth();
        final float arrowsSize = mBitmapArrows.getWidth();
        mPaperSize = mBitmapPaper.getWidth();
        mLineSize = userHorLine.getWidth();
        //
        for (int i = 0; i != GRID_SIZE; ++i) {
            mLineLocations[i] = mPaperSize / (2 * GRID_SIZE) + (i * mPaperSize) / GRID_SIZE;
            mDotLocations[i] = mLineLocations[i] - dotSize / 2.0f;
            mArrowLocations[i] = mLineLocations[i] - arrowsSize / 2.0f;
        }

        invalidate();
    }

    public void setOnMoveDoneListener(@NonNull OnMoveDoneListener listener) {
        this.mOnMoveDoneListener = listener;
    }

    public void setOnGameEndListener(@NonNull OnGameEndListener listener) {
        this.mOnGameEndListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d(TAG, "onSizeChanged");
        mScreenWidth = w;
        mScreenHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        // validate basic parameters
        if (mViewState == null || mScreenWidth <= 0 || mScreenHeight <= 0 || mPaperSize <= 0) {
            Log.d(TAG, "onDraw: something is wrong");
            return;
        }
        // correct paper whatever has happened
        mViewState.correct(mScreenWidth, mScreenHeight, mPaperSize);

        // Draw background
        canvas.drawBitmap(mBitmapPaper, mViewState.getMatrix(), null);

        // Draw dots
        for (int i = 0; i != GRID_SIZE; ++i) {
            for (int j = 0; j != GRID_SIZE; ++j) {
                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(mDotLocations[i], mDotLocations[j]);
                if (mGameState.isHostDot(i, j)) {
                    canvas.drawBitmap(mBitmapUserDot, mDrawMatrix, null);
                }
                if (mGameState.isGuestDot(i, j)) {
                    canvas.drawBitmap(mBitmapBotDot, mDrawMatrix, null);
                }
            }
        }
        // Draw dots winning line
        final ArrayList<Dot> winningLine = isOver();
        if (winningLine != null) {
            updateWinningLine();
            for (int lineId = 0; lineId != winningLine.size() - 1; ++lineId) {
                final int i = winningLine.get(lineId).getX();
                final int j = winningLine.get(lineId).getY();

                final float dx = mLineLocations[i] - mWinningLineDX;
                final float dy = mLineLocations[j] - mWinningLineDY;

                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(dx, dy);
                canvas.drawBitmap(mWinningLine, mDrawMatrix, null);
            }
        }

        // Draw four arrows
        final Dot lastDot = mGameState.getLastDot();
        if (lastDot != null) {
            final int i = lastDot.getX();
            final int j = lastDot.getY();

            mViewState.copyMatrix(mDrawMatrix);
            mDrawMatrix.preTranslate(mArrowLocations[i], mArrowLocations[j]);
            canvas.drawBitmap(mBitmapArrows, mDrawMatrix, null);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        performClick();
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    /**
     * Centers screen on the last dot
     */
    public void focus() {
        final Dot lastDot = mGameState.getLastDot();
        if (lastDot != null) {
            final float x = mLineLocations[lastDot.getX()];
            final float y = mLineLocations[lastDot.getY()];
            mViewState.focus(x, y, mScreenWidth, mScreenHeight);
            invalidate();
        }
    }

    public void newGame(@Nullable Long seed) {
        mGameState = Game.generateGame(seed);
        mViewState.focus(mPaperSize / 2.0f, mPaperSize / 2.0f, mScreenWidth, mScreenHeight);
        invalidate();
    }

    @Nullable
    private ArrayList<Dot> isOver() {
        final ArrayList<Dot> winningLine = mGameState.isOver();
        if (winningLine != null) {
            if (mOnGameEndListener != null) {
                mOnGameEndListener.onGameEnd();
            } else {
                Log.e(TAG, "listener is null");
            }
        }
        return winningLine;
    }

    /**
     * do it only when scaling or drawing
     */
    private void updateWinningLine() {
        Log.d(TAG, "updateWinningLine");
        final ArrayList<Dot> winningLine = mGameState.isOver();
        if (winningLine != null) {
            final Dot firstDot = winningLine.get(0);
            final Dot lastDot = winningLine.get(winningLine.size() - 1);

            final int x1 = firstDot.getX();
            final int y1 = firstDot.getY();
            final int x2 = lastDot.getX();
            final int y2 = lastDot.getY();

            final boolean isHostWinner = mGameState.isHostDot(x1, y1);

            if (y1 == y2) {//horizontal line
                if (isHostWinner) {
                    mWinningLine = userHorLine;
                } else {
                    mWinningLine = botHorLine;
                }
                mWinningLineDX = 0;
                mWinningLineDY = mLineSize / 2.0f;
            } else if (x1 == x2) {//vertical line
                if (isHostWinner) {
                    mWinningLine = userVerLine;
                } else {
                    mWinningLine = botVerLine;
                }
                mWinningLineDX = mLineSize / 2.0f;
                mWinningLineDY = 0;
            } else if (x1 > x2) {//diagonal left to right
                if (isHostWinner) {
                    mWinningLine = userDiagonal1Line;
                } else {
                    mWinningLine = botDiagonal1Line;
                }
                mWinningLineDX = mLineSize;
                mWinningLineDY = 0;
            } else { // (x2 > x1) diagonal right to left
                if (isHostWinner) {
                    mWinningLine = userDiagonal2Line;
                } else {
                    mWinningLine = botDiagonal2Line;
                }
                mWinningLineDX = 0;
                mWinningLineDY = 0;
            }
        }
    }

    public void setGameState(@NonNull Game game) {
        this.mGameState = game;
        if (!mViewState.isFocused()) {
            focus();
        } else {
            invalidate();
        }
    }

    @NonNull
    public GameViewState getViewState() {
        return this.mViewState;
    }

    public void setViewState(@NonNull GameViewState viewState) {
        Log.d(TAG, "setViewState");

        this.mViewState = viewState;
        onSizeChanged((int) mScreenWidth, (int) mScreenHeight,
                (int) mScreenWidth, (int) mScreenHeight);
        invalidate();
    }

    public interface OnMoveDoneListener {
        void onMoveDone(@NonNull Dot dot);
    }

    public interface OnGameEndListener {
        void onGameEnd();
    }

    private class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            mViewState.translate(-dx, -dy);
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mGameState.isOver() == null) {
                final PointF point = mViewState.invertMapPoint(e.getX(), e.getY());
                final int x = MathUtils.findClosestIndex(mLineLocations, point.x);
                final int y = MathUtils.findClosestIndex(mLineLocations, point.y);

                if (mOnMoveDoneListener != null && mGameState.checkCorrectness(x, y)) {
                    mOnMoveDoneListener.onMoveDone(new Dot(x, y));
                } else {
                    Log.e(TAG, "listener is null");
                }
            }
            return true;
        }
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.isInProgress()) {
                mViewState.scale(detector.getScaleFactor(),
                        detector.getFocusX(),
                        detector.getFocusY());
            }
            return true;
        }
    }
}