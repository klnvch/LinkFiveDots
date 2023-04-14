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

package by.klnvch.link5dots.multiplayer.utils;

import static com.google.common.base.Preconditions.checkState;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class GameState {

    private static final GameState UNDEFINED = new GameState(TargetState.NONE, ScanState.NONE, ConnectState.NONE);
    private static final GameState IDLE = new GameState(TargetState.DELETED, ScanState.OFF, ConnectState.NONE);
    private static final GameState TARGET_CREATING = new GameState(TargetState.CREATING, ScanState.NONE, ConnectState.NONE);
    private static final GameState TARGET_CREATED = new GameState(TargetState.CREATED, ScanState.NONE, ConnectState.NONE);
    private static final GameState TARGET_DELETING = new GameState(TargetState.DELETING, ScanState.NONE, ConnectState.NONE);
    private static final GameState SCAN_ON = new GameState(TargetState.NONE, ScanState.ON, ConnectState.NONE);
    private static final GameState SCAN_DONE = new GameState(TargetState.DELETED, ScanState.DONE, ConnectState.NONE);
    private static final GameState CONNECTING_TARGET = new GameState(TargetState.CREATED, ScanState.NONE, ConnectState.CONNECTING);
    private static final GameState CONNECTING_SCAN_ON = new GameState(TargetState.NONE, ScanState.ON, ConnectState.CONNECTING);
    private static final GameState CONNECTING_SCAN_DONE = new GameState(TargetState.DELETED, ScanState.DONE, ConnectState.CONNECTING);
    private static final GameState CONNECTED = new GameState(TargetState.DELETED, ScanState.OFF, ConnectState.CONNECTED);
    private static final GameState DISCONNECTED = new GameState(TargetState.DELETED, ScanState.OFF, ConnectState.DISCONNECTED);
    private static final List<Transition> mAllowedTransitions = Arrays.asList(
            new Transition(UNDEFINED, IDLE),

            new Transition(IDLE, IDLE),
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
    private TargetState targetState;
    private ScanState scanState;
    private ConnectState connectState;

    public GameState() {
        targetState = TargetState.DELETED;
        scanState = ScanState.OFF;
        connectState = ConnectState.NONE;
    }

    private GameState(TargetState targetState,
                      ScanState scanState,
                      ConnectState connectState) {
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

    @NonNull
    @Override
    public String toString() {
        return '(' + targetState.toString() + ',' + scanState + ',' + connectState + ')';
    }

    @NonNull
    public ScanState getScanState() {
        return scanState;
    }

    public void setScanState(ScanState scanState) {
        if (scanState == ScanState.ON) {
            check(new GameState(TargetState.NONE, scanState, connectState));
            this.targetState = TargetState.NONE;
        } else {
            check(new GameState(TargetState.DELETED, scanState, connectState));
            this.targetState = TargetState.DELETED;
        }
        this.scanState = scanState;
    }

    @NonNull
    public TargetState getTargetState() {
        return targetState;
    }

    public void setTargetState(TargetState targetState) {
        if (targetState == TargetState.DELETED) {
            check(new GameState(targetState, ScanState.OFF, connectState));
            this.targetState = targetState;
            this.scanState = ScanState.OFF;
        } else {
            check(new GameState(targetState, ScanState.NONE, connectState));
            this.targetState = targetState;
            this.scanState = ScanState.NONE;
        }
    }

    @NonNull
    public ConnectState getConnectState() {
        return connectState;
    }

    public void setConnectState(ConnectState connectState) {
        if (connectState == ConnectState.CONNECTED) {
            check(new GameState(TargetState.DELETED, ScanState.OFF, connectState));
            this.scanState = ScanState.OFF;
            this.targetState = TargetState.DELETED;
        } else {
            check(new GameState(targetState, scanState, connectState));
        }
        this.connectState = connectState;
    }

    public enum TargetState {NONE, DELETED, CREATING, CREATED, DELETING}

    public enum ScanState {NONE, OFF, ON, DONE}

    public enum ConnectState {NONE, CONNECTING, CONNECTED, DISCONNECTED}

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

        @NonNull
        @Override
        public String toString() {
            return mPrevState.toString() + '-' + mNextState;
        }
    }
}
