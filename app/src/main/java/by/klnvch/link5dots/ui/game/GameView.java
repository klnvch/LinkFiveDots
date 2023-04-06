/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

package by.klnvch.link5dots.ui.game;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.domain.models.Board;
import by.klnvch.link5dots.domain.models.Dot;
import by.klnvch.link5dots.domain.models.DotsStyleType;
import by.klnvch.link5dots.domain.models.Point;
import by.klnvch.link5dots.domain.models.WinningLine;
import by.klnvch.link5dots.models.GameViewState;
import by.klnvch.link5dots.utils.BitmapCreator;

public class GameView extends View {

    private static final String TAG = "GameView";
    /**
     * number of lines vertical or horizontal
     */
    private final float[] mLineLocations = new float[Board.BOARD_SIZE];
    private final float[] mDotLocations = new float[Board.BOARD_SIZE];
    private final float[] mArrowLocations = new float[Board.BOARD_SIZE];
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
    private GameViewState mViewState = new GameViewState();
    private GameBoardViewState gameBoardViewState = null;
    private OnMoveDoneListener mOnMoveDoneListener;
    private Bitmap mWinningLine = null;
    private final PointF mWinningLineD = new PointF(0, 0);

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
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
        final float arrowsSize = mBitmapArrows.getWidth();
        mPaperSize = mBitmapPaper.getWidth();
        mLineSize = userHorLine.getWidth();
        //
        for (int i = 0; i != Board.BOARD_SIZE; ++i) {
            mLineLocations[i] = mPaperSize / (2 * Board.BOARD_SIZE) + (i * mPaperSize) / Board.BOARD_SIZE;
            mArrowLocations[i] = mLineLocations[i] - arrowsSize / 2.0f;
        }

        setDotsStyleType(DotsStyleType.ORIGINAL);
    }

    public void setOnMoveDoneListener(@NonNull OnMoveDoneListener listener) {
        this.mOnMoveDoneListener = listener;
    }

    public void setDotsStyleType(@Nullable DotsStyleType dotsType) {
        final float density = getResources().getDisplayMetrics().density;
        final int colorRed = ContextCompat.getColor(getContext(), R.color.dot_color_red);
        final int colorBlue = ContextCompat.getColor(getContext(), R.color.dot_color_blue);
        if (dotsType == DotsStyleType.ORIGINAL) {
            mBitmapUserDot = BitmapCreator.createBitmap(BitmapCreator.DOT, colorRed, density);
            mBitmapBotDot = BitmapCreator.createBitmap(BitmapCreator.DOT, colorBlue, density);
        } else {
            mBitmapUserDot = BitmapCreator.createBitmap(BitmapCreator.CROSS, colorRed, density);
            mBitmapBotDot = BitmapCreator.createBitmap(BitmapCreator.RING, colorBlue, density);
        }
        final float dotSize = mBitmapUserDot.getWidth();
        for (int i = 0; i != Board.BOARD_SIZE; ++i) {
            mDotLocations[i] = mLineLocations[i] - dotSize / 2.0f;
        }
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
        if (gameBoardViewState != null) {
            for (Dot dot : gameBoardViewState.getDots()) {
                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(mDotLocations[dot.getX()], mDotLocations[dot.getY()]);
                if (dot.getType() == Dot.HOST) {
                    canvas.drawBitmap(mBitmapUserDot, mDrawMatrix, null);
                } else if (dot.getType() == Dot.GUEST) {
                    canvas.drawBitmap(mBitmapBotDot, mDrawMatrix, null);
                }
            }
        }
        // Draw dots winning line
        if (gameBoardViewState != null && gameBoardViewState.getWinningLine() != null) {
            updateWinningLine();
            final WinningLine winningLine = gameBoardViewState.getWinningLine();
            for (int lineId = 0; lineId != winningLine.getSize() - 1; ++lineId) {
                final int i = winningLine.get(lineId).getX();
                final int j = winningLine.get(lineId).getY();

                final float dx = mLineLocations[i] - mWinningLineD.x;
                final float dy = mLineLocations[j] - mWinningLineD.y;

                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(dx, dy);
                canvas.drawBitmap(mWinningLine, mDrawMatrix, null);
            }
        }
        // Draw four arrows
        if (gameBoardViewState != null && gameBoardViewState.getLastDot() != null) {
            final Dot lastDot = gameBoardViewState.getLastDot();
            if (lastDot != null) {
                final int i = lastDot.getX();
                final int j = lastDot.getY();

                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(mArrowLocations[i], mArrowLocations[j]);
                canvas.drawBitmap(mBitmapArrows, mDrawMatrix, null);
            }
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
        if (gameBoardViewState != null && gameBoardViewState.getLastDot() != null) {
            final Dot lastDot = gameBoardViewState.getLastDot();
            if (lastDot != null) {
                final float x = mLineLocations[lastDot.getX()];
                final float y = mLineLocations[lastDot.getY()];
                mViewState.focus(x, y, mScreenWidth, mScreenHeight);
                invalidate();
            }
        }
    }

    public void reset() {
        mViewState.focus(mPaperSize / 2.0f, mPaperSize / 2.0f, mScreenWidth, mScreenHeight);
        invalidate();
    }

    /**
     * do it only when scaling or drawing
     */
    private void updateWinningLine() {
        if (gameBoardViewState != null && gameBoardViewState.getWinningLine() != null) {
            final WinningLine winningLine = gameBoardViewState.getWinningLine();
            final boolean isHostWinner = winningLine.getType() == Dot.HOST;

            switch (winningLine.getOrientation()) {
                case HORIZONTAL:
                    mWinningLine = isHostWinner ? userHorLine : botHorLine;
                    mWinningLineD.set(0, mLineSize / 2.0f);
                    break;
                case VERTICAL:
                    mWinningLine = isHostWinner ? userVerLine : botVerLine;
                    mWinningLineD.set(mLineSize / 2.0f, 0);
                    break;
                case DIAGONAL_LEFT:
                    mWinningLine = isHostWinner ? userDiagonal1Line : botDiagonal1Line;
                    mWinningLineD.set(0, mLineSize);
                    break;
                case DIAGONAL_RIGHT:
                    mWinningLine = isHostWinner ? userDiagonal2Line : botDiagonal2Line;
                    mWinningLineD.set(0, 0);
                    break;
            }
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

    public void setGameBoardViewState(@Nullable GameBoardViewState gameBoardViewState) {
        this.gameBoardViewState = gameBoardViewState;
        setDotsStyleType(gameBoardViewState != null ? gameBoardViewState.getDotsStyleType() : null);
        if (!mViewState.isFocused()) {
            focus();
        } else {
            invalidate();
        }
    }

    public int findClosestIndex(float value) {
        int result = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i != mLineLocations.length; ++i) {
            final float dist = Math.abs(value - mLineLocations[i]);
            if (min > dist) {
                min = dist;
                result = i;
            }
        }
        return result;
    }

    public interface OnMoveDoneListener {
        void onMoveDone(@NonNull Point dot);
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
            final PointF point = mViewState.invertMapPoint(e.getX(), e.getY());
            final int x = findClosestIndex(point.x);
            final int y = findClosestIndex(point.y);

            if (mOnMoveDoneListener != null) {
                mOnMoveDoneListener.onMoveDone(new Point(x, y));
            } else {
                Log.e(TAG, "listener is null");
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