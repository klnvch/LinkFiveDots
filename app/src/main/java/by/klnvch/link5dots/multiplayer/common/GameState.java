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

package by.klnvch.link5dots.multiplayer.common;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class GameState {
    public static final int STATE_NONE = -1;

    public static final int STATE_TARGET_DELETED = 0;
    public static final int STATE_TARGET_CREATING = 1;
    public static final int STATE_TARGET_CREATED = 2;
    public static final int STATE_TARGET_DELETING = 3;

    public static final int STATE_SCAN_OFF = 4;
    public static final int STATE_SCAN_ON = 5;
    public static final int STATE_SCAN_DONE = 6;

    public static final int STATE_CONNECTING = 7;
    public static final int STATE_CONNECTED = 8;
    public static final int STATE_DISCONNECTED = 9;

    private static final GameState UNDEFINED = new GameState(STATE_NONE, STATE_NONE, STATE_NONE);

    private static final GameState IDLE = new GameState(STATE_TARGET_DELETED, STATE_SCAN_OFF, STATE_NONE);

    private static final GameState TARGET_CREATING = new GameState(STATE_TARGET_CREATING, STATE_NONE, STATE_NONE);
    private static final GameState TARGET_CREATED = new GameState(STATE_TARGET_CREATED, STATE_NONE, STATE_NONE);
    private static final GameState TARGET_DELETING = new GameState(STATE_TARGET_DELETING, STATE_NONE, STATE_NONE);

    private static final GameState SCAN_ON = new GameState(STATE_NONE, STATE_SCAN_ON, STATE_NONE);
    private static final GameState SCAN_DONE = new GameState(STATE_TARGET_DELETED, STATE_SCAN_DONE, STATE_NONE);

    private static final GameState CONNECTING_TARGET = new GameState(STATE_TARGET_CREATED, STATE_NONE, STATE_CONNECTING);
    private static final GameState CONNECTING_SCAN_ON = new GameState(STATE_NONE, STATE_SCAN_ON, STATE_CONNECTING);
    private static final GameState CONNECTING_SCAN_DONE = new GameState(STATE_TARGET_DELETED, STATE_SCAN_DONE, STATE_CONNECTING);

    private static final GameState CONNECTED = new GameState(STATE_TARGET_DELETED, STATE_SCAN_OFF, STATE_CONNECTED);
    private static final GameState DISCONNECTED = new GameState(STATE_TARGET_DELETED, STATE_SCAN_OFF, STATE_DISCONNECTED);


    private static final List<Transition> mAllowedTransitions = Arrays.asList(
            new Transition(UNDEFINED, IDLE),

            new Transition(IDLE, TARGET_CREATING),
            new Transition(IDLE, SCAN_ON),

            new Transition(TARGET_CREATING, TARGET_CREATED),
            new Transition(TARGET_CREATING, IDLE),

            new Transition(TARGET_CREATED, IDLE),
            new Transition(TARGET_CREATED, TARGET_CREATED),
            new Transition(TARGET_CREATED, TARGET_DELETING),
            new Transition(TARGET_CREATED, CONNECTING_TARGET),
            new Transition(TARGET_CREATED, CONNECTED),

            new Transition(TARGET_DELETING, IDLE),

            new Transition(SCAN_ON, IDLE),
            new Transition(SCAN_ON, SCAN_DONE),
            new Transition(SCAN_ON, CONNECTING_SCAN_ON),

            new Transition(SCAN_DONE, IDLE),
            new Transition(SCAN_DONE, TARGET_CREATING),
            new Transition(SCAN_DONE, SCAN_ON),
            new Transition(SCAN_DONE, CONNECTING_SCAN_DONE),

            new Transition(CONNECTING_TARGET, TARGET_CREATED),
            new Transition(CONNECTING_TARGET, CONNECTED),

            new Transition(CONNECTING_SCAN_ON, SCAN_ON),
            new Transition(CONNECTING_SCAN_ON, CONNECTED),
            new Transition(CONNECTING_SCAN_ON, CONNECTING_SCAN_DONE),

            new Transition(CONNECTING_SCAN_DONE, SCAN_DONE),
            new Transition(CONNECTING_SCAN_DONE, CONNECTED),

            new Transition(CONNECTED, DISCONNECTED),
            new Transition(CONNECTED, CONNECTED),
            new Transition(CONNECTED, IDLE),

            new Transition(DISCONNECTED, DISCONNECTED),
            new Transition(DISCONNECTED, IDLE)
    );

    private int targetState;
    private int scanState;
    private int connectState;

    public GameState() {
        targetState = STATE_TARGET_DELETED;
        scanState = STATE_SCAN_OFF;
        connectState = STATE_NONE;
    }

    private GameState(@TargetState int targetState,
                      @ScanState int scanState,
                      @ConnectState int connectState) {
        this.targetState = targetState;
        this.scanState = scanState;
        this.connectState = connectState;
    }

    private void check(@NonNull GameState next) {
        final Transition transition = new Transition(this, next);
        checkState(mAllowedTransitions.contains(transition), "error: " + transition);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GameState) {
            final GameState state = (GameState) obj;
            return state.targetState == targetState
                    && state.scanState == scanState
                    && state.connectState == connectState;
        }
        return false;
    }

    @Override
    public String toString() {
        return '(' + Integer.toString(targetState) + ','
                + Integer.toString(scanState) + ','
                + Integer.toString(connectState) + ')';
    }

    @ScanState
    int getScanState() {
        return scanState;
    }

    public void setScanState(@ScanState int scanState) {
        if (scanState == STATE_SCAN_ON) {
            check(new GameState(STATE_NONE, scanState, connectState));
            this.targetState = STATE_NONE;
            this.scanState = scanState;
        } else {
            check(new GameState(STATE_TARGET_DELETED, scanState, connectState));
            this.targetState = STATE_TARGET_DELETED;
            this.scanState = scanState;
        }
    }

    @TargetState
    int getTargetState() {
        return targetState;
    }

    public void setTargetState(@TargetState int targetState) {
        if (targetState == STATE_TARGET_DELETED) {
            check(new GameState(targetState, STATE_SCAN_OFF, connectState));
            this.targetState = targetState;
            this.scanState = STATE_SCAN_OFF;
        } else {
            check(new GameState(targetState, STATE_NONE, connectState));
            this.targetState = targetState;
            this.scanState = STATE_NONE;
        }
    }

    @ConnectState
    int getConnectState() {
        return connectState;
    }

    public void setConnectState(@ConnectState int connectState) {
        if (connectState == STATE_CONNECTED) {
            check(new GameState(STATE_TARGET_DELETED, STATE_SCAN_OFF, connectState));
            this.scanState = STATE_SCAN_OFF;
            this.targetState = STATE_TARGET_DELETED;
            this.connectState = connectState;
        } else {
            check(new GameState(targetState, scanState, connectState));
            this.connectState = connectState;
        }
    }

    @Retention(SOURCE)
    @IntDef({STATE_NONE, STATE_TARGET_DELETED, STATE_TARGET_CREATING, STATE_TARGET_CREATED, STATE_TARGET_DELETING})
    public @interface TargetState {
    }

    @Retention(SOURCE)
    @IntDef({STATE_NONE, STATE_SCAN_OFF, STATE_SCAN_ON, STATE_SCAN_DONE})
    public @interface ScanState {
    }

    @Retention(SOURCE)
    @IntDef({STATE_NONE, STATE_CONNECTING, STATE_CONNECTED, STATE_DISCONNECTED})
    public @interface ConnectState {
    }

    private static class Transition {
        private final GameState mPrevState;
        private final GameState mNextState;

        private Transition(@NonNull GameState prevState, @NonNull GameState nextState) {
            this.mPrevState = prevState;
            this.mNextState = nextState;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Transition) {
                final Transition transition = (Transition) obj;
                return transition.mPrevState.equals(mPrevState)
                        && transition.mNextState.equals(mNextState);
            }
            return false;
        }

        @Override
        public String toString() {
            return mPrevState.toString() + '-' + mNextState.toString();
        }
    }
}