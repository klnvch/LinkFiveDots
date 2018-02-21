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
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
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
import by.klnvch.link5dots.settings.SettingsUtils;
import by.klnvch.link5dots.utils.BitmapCreator;

public class GameView extends View {

    private static final String TAG = "GameView";
    /**
     * number of lines vertical or horizontal
     */
    private final static int GRID_SIZE = 20;
    private final float[] arr1 = new float[GRID_SIZE];
    private final float[] dotLocations = new float[GRID_SIZE];
    private final float[] arrowLocations = new float[GRID_SIZE];
    private final Matrix mDrawMatrix = new Matrix();
    /**
     * The background bitmap is paper size pixels wide. This array contains positions of lines of number GRID_SIZE
     */
    private float screenWidth;
    private float screenHeight;
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
    private float paperSize;
    private float lineLength;
    private float lineThickness;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private Game mGameState = Game.generateGame(null);
    private GameViewState mViewState = new GameViewState();
    private OnMoveDoneListener onMoveDoneListener;
    private OnGameEndListener onGameEndListener;
    private Bitmap mWinningLine = null;
    private float mWinningLineDX = 0;
    private float mWinningLineDY = 0;
    private boolean isEndGameSend = false;

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
        Log.d(TAG, "initGameView");

        setFocusable(true);
        setFocusableInTouchMode(true);

        // set gesture detectors
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        // load bitmaps. why do we need matrix?
        final float density = getResources().getDisplayMetrics().density;
        mBitmapPaper = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        if (SettingsUtils.getDotsType(getContext()) == SettingsUtils.DOTS_TYPE_ORIGINAL) {
            mBitmapUserDot = BitmapCreator.createDot(Color.RED, density);
            mBitmapBotDot = BitmapCreator.createDot(Color.BLUE, density);
        } else {
            mBitmapUserDot = BitmapCreator.createCross(Color.RED, density);
            mBitmapBotDot = BitmapCreator.createRing(Color.BLUE, density);
        }
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

        mBitmapArrows = BitmapFactory.decodeResource(getResources(), R.drawable.arrows);

        // set bitmap sizes
        final float dotSize = mBitmapUserDot.getWidth();
        final float arrowsSize = mBitmapArrows.getWidth();
        paperSize = mBitmapPaper.getWidth();
        lineLength = userHorLine.getWidth();
        lineThickness = userHorLine.getHeight();
        //
        for (int i = 0; i != GRID_SIZE; ++i) {
            arr1[i] = paperSize / (2 * GRID_SIZE) + (i * paperSize) / GRID_SIZE;
            dotLocations[i] = arr1[i] - dotSize / 2.0f;
            arrowLocations[i] = arr1[i] - arrowsSize / 2.0f;
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
        Log.d(TAG, "onSizeChanged");
        screenWidth = w;
        screenHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        // validate basic parameters
        if (mViewState == null || screenWidth <= 0 || screenHeight <= 0 || paperSize <= 0) {
            Log.d(TAG, "onDraw: something is wrong");
            return;
        }
        // correct paper whatever has happened
        mViewState.correctParameters(screenWidth, screenHeight, paperSize);

        // Draw background
        canvas.drawBitmap(mBitmapPaper, mViewState.getMatrix(), null);

        // Draw dots
        for (int i = 0; i != GRID_SIZE; ++i) {
            for (int j = 0; j != GRID_SIZE; ++j) {
                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(dotLocations[i], dotLocations[j]);
                if (mGameState.isHostDot(i, j)) {
                    canvas.drawBitmap(mBitmapUserDot, mDrawMatrix, null);
                }
                if (mGameState.isGuestDot(i, j)) {
                    canvas.drawBitmap(mBitmapBotDot, mDrawMatrix, null);
                }
            }
        }
        // Draw dots winning line
        isOver();
        ArrayList<Dot> winningLine = mGameState.isOver();
        if (winningLine != null) {
            for (int lineId = 0; lineId != winningLine.size() - 1; ++lineId) {
                int i = winningLine.get(lineId).getX();
                int j = winningLine.get(lineId).getY();

                updateWinningLine();
                mViewState.copyMatrix(mDrawMatrix);
                mDrawMatrix.preTranslate(arr1[i] - mWinningLineDX, arr1[j] - mWinningLineDY);
                canvas.drawBitmap(mWinningLine, mDrawMatrix, null);
            }
        }

        // Draw four arrows
        Dot lastDot = mGameState.getLastDot();
        if (lastDot != null && mViewState.isFocusVisible) {
            int i = lastDot.getX();
            int j = lastDot.getY();

            mViewState.copyMatrix(mDrawMatrix);
            mDrawMatrix.preTranslate(arrowLocations[i], arrowLocations[j]);
            canvas.drawBitmap(mBitmapArrows, mDrawMatrix, null);
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
        isEndGameSend = false;
        mGameState.undo(moves);
        invalidate();
    }

    public void newGame(@Nullable Integer seed) {
        isEndGameSend = false;
        mGameState = Game.generateGame(seed);
        invalidate();
    }

    private int findClosestLine(float line) {
        int result = 0;
        float min = Float.MAX_VALUE;
        for (int i = 0; i != GRID_SIZE; ++i) {
            float dist = Math.abs(line - arr1[i]);
            if (min > dist) {
                min = dist;
                result = i;
            }
        }
        return result;
    }

    @Nullable
    public Dot setHostDot(@NonNull Dot dot) {
        Dot result = mGameState.setHostDot(dot);
        invalidate();
        return result;
    }

    @Nullable
    public Dot setGuestDot(@NonNull Dot dot) {
        Dot result = mGameState.setGuestDot(dot);
        invalidate();
        return result;
    }

    private void isOver() {
        if (mGameState.isOver() != null) {
            HighScore highScore = mGameState.getCurrentScore();
            if (highScore != null) {
                if (onGameEndListener != null) {
                    if (!isEndGameSend) {
                        isEndGameSend = true;
                        onGameEndListener.onGameEnd(highScore);
                    }
                } else {
                    Log.e(TAG, "listener is null");
                }
            }
        }
    }

    public HighScore getHighScore() {
        return mGameState.getCurrentScore();
    }

    /**
     * do it only when scaling or drawing
     */
    private void updateWinningLine() {
        Log.d(TAG, "updateWinningLine");
        ArrayList<Dot> winningLine = mGameState.isOver();
        if (winningLine != null) {
            Dot firstDot = winningLine.get(0);
            Dot lastDot = winningLine.get(winningLine.size() - 1);

            int x1 = firstDot.getX();
            int y1 = firstDot.getY();
            int x2 = lastDot.getX();
            int y2 = lastDot.getY();

            boolean isHostWinner = mGameState.isHostDot(x1, y1);

            if (y1 == y2) {//horizontal line
                if (isHostWinner) {
                    mWinningLine = userHorLine;
                } else {
                    mWinningLine = botHorLine;
                }
                mWinningLineDX = 0;
                mWinningLineDY = lineThickness / 2.0f;
            } else if (x1 == x2) {//vertical line
                if (isHostWinner) {
                    mWinningLine = userVerLine;
                } else {
                    mWinningLine = botVerLine;
                }
                mWinningLineDX = lineThickness / 2.0f;
                mWinningLineDY = 0;
            } else if (x1 > x2) {//diagonal left to right
                if (isHostWinner) {
                    mWinningLine = userDiagonal1Line;
                } else {
                    mWinningLine = botDiagonal1Line;
                }
                mWinningLineDX = lineLength;
                mWinningLineDY = 0;
            } else if (x2 > x1) {//diagonal right to left
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

    @NonNull
    public Game getGameState() {
        return this.mGameState;
    }

    public void setGameState(@NonNull Game game) {
        this.mGameState = game;
        invalidate();
    }

    @NonNull
    public GameViewState getViewState() {
        return this.mViewState;
    }

    public void setViewState(@NonNull GameViewState viewState) {
        Log.d(TAG, "setViewState");

        this.mViewState = viewState;
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
            mViewState.translate(-dx, -dy);
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mGameState.isOver() == null) {
                PointF point = mViewState.invertMapPoint(e.getX(), e.getY());
                int xl = findClosestLine(point.x);
                int yl = findClosestLine(point.y);
                boolean res = mGameState.checkCorrectness(xl, yl);
                if (!res) {
                    return true;
                }

                if (onMoveDoneListener != null) {
                    onMoveDoneListener.onMoveDone(new Dot(xl, yl), mGameState.getLastDot());
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