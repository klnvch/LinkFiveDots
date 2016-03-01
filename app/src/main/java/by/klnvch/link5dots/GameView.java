package by.klnvch.link5dots;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

import java.util.ArrayList;

public class GameView extends View {

    private static final String TAG = "GameView";
    /**
     * number of lines vertical or horizontal
     */
    private final static int GRID_SIZE = 20;
    /**
     * Displacement of the left top corner of the bitmap from the left top corner of the screen
     */
    private PointF basePoint;
    private float w;                //width of the screen
    private float h;                //height of the screen
    /**
     * Scale to apply to the base point and arr1 after scaling
     */
    private float scale;
    private float minScale;
    private float maxScale;
    /**
     * Used for displaying background
     * <p>
     * onSizeChanged				translate
     * onScale						scale
     * correctMatrixAndBasePoint	translate
     * onScroll					translate
     */
    private Matrix matrix;

    private boolean hideArrows;
    /*
     * The background bitmap is paper size pixels wide. This array contains positions of lines of number GRID_SIZE
     */
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

    private Game mGame;
    private OnGameEventListener listener;
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
        //
        matrix = new Matrix();
        this.mGame = new Game("host", "guest");
    }

    public void setOnGameEventListener(OnGameEventListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {

        //set base point if it is not set yet
        if (this.w == 0 || this.h == 0) {//there is nothing to restore. it is the first run
            //set width and height
            this.w = w;
            this.h = h;

            float dx = (w - paperSize) / 2.0f;
            float dy = (h - paperSize) / 2.0f;
            basePoint = new PointF(dx, dy);
            matrix.postTranslate(dx, dy);

            onScale(scale, w / 2.0f, h / 2.0f);

        } else {
            //set base point and matrix if screen sizes have changed

            float dx = (w - this.w) / 2.0f;
            float dy = (h - this.h) / 2.0f;
            basePoint.x += dx;
            basePoint.y += dy;
            matrix.postTranslate(dx, dy);

            //set new width and height
            this.w = w;
            this.h = h;

            onScale(1.0f, w / 2.0f, h / 2.0f);
        }

        super.onSizeChanged(w, h, oldW, oldH);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Draw background
        canvas.drawBitmap(background, matrix, null);

        // Draw dots
        for (int i = 0; i != GRID_SIZE; ++i) {
            for (int j = 0; j != GRID_SIZE; ++j) {
                float dX = basePoint.x + arr1[i] * scale - dotSize * scale / 2.0f;
                float dY = basePoint.y + arr1[j] * scale - dotSize * scale / 2.0f;
                if (mGame.net[i][j].getType() == Dot.USER) {
                    canvas.drawBitmap(scaledUserDot, dX, dY, null);
                }
                if (mGame.net[i][j].getType() == Dot.OPPONENT) {
                    canvas.drawBitmap(scaledBotDot, dX, dY, null);
                }
            }
        }
        // Draw dots winning line
        ArrayList<Dot> winningLine = mGame.isOver();
        if (winningLine != null) {
            Dot firstDot = winningLine.get(0);
            Dot lastDot = winningLine.get(winningLine.size() - 1);

            float x1 = basePoint.x + arr1[firstDot.getX()] * scale;
            float y1 = basePoint.y + arr1[firstDot.getY()] * scale;
            float x2 = basePoint.x + arr1[lastDot.getX()] * scale;
            float y2 = basePoint.y + arr1[lastDot.getY()] * scale;


            float shiftX = 0.0f;
            float shiftY = 0.0f;

            if (y1 == y2) {//horizontal line
                shiftY = lineThickness * scale / 2.0f;
            } else if (x1 == x2) {//vertical line
                shiftX = lineThickness * scale / 2.0f;
            } else if (x1 > x2) {//diagonal left to right
                shiftX = lineLength * scale;
            } else if (x2 > x1) {//diagonal right to left
            }

            for (int i = 0; i != winningLine.size() - 1; ++i) {
                float dX = basePoint.x + arr1[winningLine.get(i).getX()] * scale - shiftX;
                float dY = basePoint.y + arr1[winningLine.get(i).getY()] * scale - shiftY;
                canvas.drawBitmap(newline, dX, dY, null);
            }
        }

        // Draw four arrows

        Dot lastDot = mGame.getLastDot();

        if (lastDot != null && !hideArrows) {
            float dX = basePoint.x + arr1[lastDot.getX()] * scale - (arrowsSize * scale) / 2.0f;
            float dY = basePoint.y + arr1[lastDot.getY()] * scale - (arrowsSize * scale) / 2.0f;
            canvas.drawBitmap(scaledArrow, dX, dY, null);

        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public void switchHideArrow() {
        hideArrows = !hideArrows;
        invalidate();
    }

    public void undoLastMove(int moves) {
        mGame.undo(moves);
        invalidate();
    }

    public void resetGame() {
        mGame.reset();
        invalidate();
    }

    public Dot[][] getCopyOfNet() {
        Dot[][] copyNet = new Dot[mGame.net.length][];
        for (int i = 0; i != mGame.net.length; ++i) {
            copyNet[i] = new Dot[mGame.net[i].length];
            for (int j = 0; j != mGame.net[i].length; ++j) {
                copyNet[i][j] = mGame.net[i][j].copy();
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
            float dist = Math.abs(y - basePoint.y - arr1[i] * scale);
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
            float dist = Math.abs(x - basePoint.x - arr1[i] * scale);
            if (min > dist) {
                min = dist;
                result = i;
            }
        }
        return result;
    }

    public void setDot(Dot dot) {

        mGame.setDot(dot.getX(), dot.getY(), dot.getType());
        invalidate();

        //
        isOver();
    }

    public void isOver() {
        if (mGame.isOver() != null) {
            updateLinesBitmaps();
            HighScore highScore = mGame.getCurrentScore();
            if (highScore != null) {
                if (listener != null) {
                    listener.onGameEnd(highScore);
                } else {
                    Log.e(TAG, "listener is null");
                }
            }
        }
    }

    private void onScale(float scaleFactor, float px, float py) {

        float newScale = scale * scaleFactor;

        //in case new scale < minimal possible scale < current scale, increase new scale
        //check it in case if the image is less than screen
        //it is necessary to align image to the screen borders in any case and minimize as much as possible
        if (paperSize * newScale < h && paperSize * newScale < w) {
            float minPosScale = Math.min(h / paperSize, w / paperSize);
            if (minPosScale > newScale) {
                newScale = minPosScale;
                scaleFactor = newScale / scale;
            }
            if (minPosScale > maxScale) {
                maxScale = minPosScale;
            }
        }

        //check if it is allowed to scale
        if ((newScale < minScale && scaleFactor < 1.0f) || (newScale > maxScale && scaleFactor > 1.0f))
            return;


        if (paperSize * newScale >= h || paperSize * newScale >= w || scaleFactor > 1.0f) {

            //scale game_board ground
            matrix.postScale(scaleFactor, scaleFactor, px, py);
            updateScale(newScale);
            basePoint.x += (1 - scaleFactor) * (px - basePoint.x);
            basePoint.y += (1 - scaleFactor) * (py - basePoint.y);

            //move it
            //center x if the bitmap width is less than the screen width
            correctMatrixAndBasePoint();

            invalidate();
        }
    }

    private void updateScale(float newScale) {
        scale = newScale;

        scaledBotDot = Bitmap.createScaledBitmap(botDot, (int) (dotSize * scale), (int) (dotSize * scale), true);
        scaledUserDot = Bitmap.createScaledBitmap(userDot, (int) (dotSize * scale), (int) (dotSize * scale), true);
        updateLinesBitmaps();
        scaledArrow = Bitmap.createScaledBitmap(arrows, (int) (arrowsSize * scale), (int) (arrowsSize * scale), true);
    }

    private void updateLinesBitmaps() {
        ArrayList<Dot> winningLine = mGame.isOver();
        if (winningLine != null) {
            Dot firstDot = winningLine.get(0);
            Dot lastDot = winningLine.get(winningLine.size() - 1);

            float x1 = basePoint.x + arr1[firstDot.getX()] * scale;
            float y1 = basePoint.y + arr1[firstDot.getY()] * scale;
            float x2 = basePoint.x + arr1[lastDot.getX()] * scale;
            float y2 = basePoint.y + arr1[lastDot.getY()] * scale;

            if (y1 == y2) {//horizontal line
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userHorLine, (int) (lineLength * scale), (int) (lineThickness * scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botHorLine, (int) (lineLength * scale), (int) (lineThickness * scale), true);
                }
            } else if (x1 == x2) {//vertical line
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userVerLine, (int) (lineThickness * scale), (int) (lineLength * scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botVerLine, (int) (lineThickness * scale), (int) (lineLength * scale), true);
                }
            } else if (x1 > x2) {//diagonal left to right
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userDiagonal1Line, (int) (lineLength * scale), (int) (lineLength * scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botDiagonal1Line, (int) (lineLength * scale), (int) (lineLength * scale), true);
                }
            } else if (x2 > x1) {//diagonal right to left
                if (firstDot.getType() == Dot.USER) {
                    newline = Bitmap.createScaledBitmap(userDiagonal2Line, (int) (lineLength * scale), (int) (lineLength * scale), true);
                } else {
                    newline = Bitmap.createScaledBitmap(botDiagonal2Line, (int) (lineLength * scale), (int) (lineLength * scale), true);
                }
            }
        }
    }

    private void correctMatrixAndBasePoint() {
        if (paperSize * scale <= w) {
            float newBaseX = (w - paperSize * scale) / 2.0f;
            matrix.postTranslate(newBaseX - basePoint.x, 0);
            basePoint.x = newBaseX;
        }
        //move left if bitmap is too right
        else if (basePoint.x > 0.0f) {
            matrix.postTranslate(-basePoint.x, 0);
            basePoint.x = 0.0f;
        }
        //move right if bitmap is too left
        else if (w > basePoint.x + paperSize * scale) {
            float dx = w - (basePoint.x + paperSize * scale);
            matrix.postTranslate(dx, 0);
            basePoint.x += dx;
        }

        //center y if the bitmap height is less than the screen height
        if (paperSize * scale <= h) {
            float newBaseY = (h - paperSize * scale) / 2.0f;
            matrix.postTranslate(0, newBaseY - basePoint.y);
            basePoint.y = newBaseY;
        }
        //move up if bitmap is too low
        else if (basePoint.y > 0.0f) {
            matrix.postTranslate(0, -basePoint.y);
            basePoint.y = 0.0f;
        }
        //move down if bitmap is too high
        else if (h > basePoint.y + paperSize * scale) {
            float dy = h - (basePoint.y + paperSize * scale);
            matrix.postTranslate(0, dy);
            basePoint.y += dy;
        }
    }

    public void restore(SharedPreferences pref) {
        mGame.restore(pref);

        float defScale;
        defScale = 1.0f;
        minScale = 0.0f;
        maxScale = 3.0f;

        hideArrows = pref.getBoolean("hideArrows", false);
        float x = pref.getFloat("basepoint_x", 0);
        float y = pref.getFloat("basepoint_y", 0);
        basePoint = new PointF(x, y);                //must be initialized at first run
        updateScale(pref.getFloat("scale", defScale));
        w = pref.getFloat("screen_width", 0);    //must be initialized at first run
        h = pref.getFloat("screen_height", 0);  //must be initialized at first run
        float[] arr = new float[9];
        arr[0] = pref.getFloat("m0", 1);
        arr[1] = pref.getFloat("m1", 0);
        arr[2] = pref.getFloat("m2", 0);
        arr[3] = pref.getFloat("m3", 0);
        arr[4] = pref.getFloat("m4", 1);
        arr[5] = pref.getFloat("m5", 0);
        arr[6] = pref.getFloat("m6", 0);
        arr[7] = pref.getFloat("m7", 0);
        arr[8] = pref.getFloat("m8", 1);
        matrix.setValues(arr);
    }

    public void restore(Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        mGame.restore(bundle);

        float defScale;
        defScale = 1.0f;
        minScale = 0.0f;
        maxScale = 3.0f;

        hideArrows = bundle.getBoolean("hideArrows", false);
        float x = bundle.getFloat("basepoint_x", 0);
        float y = bundle.getFloat("basepoint_y", 0);
        basePoint = new PointF(x, y);                //must be initialized at first run
        updateScale(bundle.getFloat("scale", defScale));
        w = bundle.getFloat("screen_width", 0);    //must be initialized at first run
        h = bundle.getFloat("screen_height", 0);  //must be initialized at first run
        float[] arr = new float[9];
        arr[0] = bundle.getFloat("m0", 1);
        arr[1] = bundle.getFloat("m1", 0);
        arr[2] = bundle.getFloat("m2", 0);
        arr[3] = bundle.getFloat("m3", 0);
        arr[4] = bundle.getFloat("m4", 1);
        arr[5] = bundle.getFloat("m5", 0);
        arr[6] = bundle.getFloat("m6", 0);
        arr[7] = bundle.getFloat("m7", 0);
        arr[8] = bundle.getFloat("m8", 1);
        matrix.setValues(arr);
    }

    public void save(SharedPreferences pref) {
        mGame.save(pref);

        Editor editor = pref.edit();
        editor.putBoolean("hideArrows", hideArrows);
        editor.putFloat("scale", scale);
        editor.putFloat("basepoint_x", basePoint.x);
        editor.putFloat("basepoint_y", basePoint.y);
        editor.putFloat("screen_width", w);
        editor.putFloat("screen_height", h);
        float[] arr = new float[9];
        matrix.getValues(arr);
        editor.putFloat("m0", arr[0]);
        editor.putFloat("m1", arr[1]);
        editor.putFloat("m2", arr[2]);
        editor.putFloat("m3", arr[3]);
        editor.putFloat("m4", arr[4]);
        editor.putFloat("m5", arr[5]);
        editor.putFloat("m6", arr[6]);
        editor.putFloat("m7", arr[7]);
        editor.putFloat("m8", arr[8]);
        editor.apply();
    }

    public void save(Bundle bundle) {

        mGame.save(bundle);

        bundle.putBoolean("hideArrows", hideArrows);
        bundle.putFloat("scale", scale);
        bundle.putFloat("basepoint_x", basePoint.x);
        bundle.putFloat("basepoint_y", basePoint.y);
        bundle.putFloat("screen_width", w);
        bundle.putFloat("screen_height", h);
        float[] arr = new float[9];
        matrix.getValues(arr);
        bundle.putFloat("m0", arr[0]);
        bundle.putFloat("m1", arr[1]);
        bundle.putFloat("m2", arr[2]);
        bundle.putFloat("m3", arr[3]);
        bundle.putFloat("m4", arr[4]);
        bundle.putFloat("m5", arr[5]);
        bundle.putFloat("m6", arr[6]);
        bundle.putFloat("m7", arr[7]);
        bundle.putFloat("m8", arr[8]);
    }

    public interface OnGameEventListener {
        void onMoveDone(Dot currentDot, Dot previousDot);

        void onGameEnd(HighScore highScore);
    }

    private class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {

            float newBaseX = basePoint.x - dx;
            float newBaseY = basePoint.y - dy;
            //check if the new position of the bitmap is in the screen
            if (newBaseX <= 0.0f && newBaseX + paperSize * scale >= w) {

                basePoint.x = newBaseX;
                matrix.postTranslate(-dx, 0);
            }
            if (newBaseY <= 0.0f && newBaseY + paperSize * scale >= h) {

                basePoint.y = newBaseY;
                matrix.postTranslate(0, -dy);
            }
            invalidate();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mGame.isOver() == null) {
                int xl = findTapedVerLine(e.getX());
                int yl = findTapedHorLine(e.getY());
                boolean res = mGame.checkCorrectness(xl, yl);
                if (!res) {
                    return true;
                }

                invalidate();

                if (listener != null) {
                    listener.onMoveDone(new Dot(xl, yl), mGame.getLastDot());
                } else {
                    Log.e(TAG, "listener is null");
                }

                //
                if (mGame.isOver() != null) {
                    updateLinesBitmaps();
                    HighScore highScore = mGame.getCurrentScore();

                    if (listener != null) {
                        listener.onGameEnd(highScore);
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
