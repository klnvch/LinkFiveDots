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

package by.klnvch.link5dots.multiplayer.services;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.threads.AcceptThread;
import by.klnvch.link5dots.multiplayer.threads.ConnectThread;
import by.klnvch.link5dots.multiplayer.threads.ConnectedThread;
import by.klnvch.link5dots.multiplayer.threads.OnSocketConnectedListener;
import by.klnvch.link5dots.multiplayer.utils.GameState;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Server:
 * <p>
 * Create game:
 * 1. createTarget
 * 2. AcceptThread.start
 * 3. onServerSocketCreated or onServerSocketFailed
 * 4. mRegistrationTask.registerService or mVisibilityTimer.start
 * 5. onTargetCreated or onTargetCreationFailed
 * <p>
 * Delete game:
 * 1. deleteTarget
 * 2. mRegistrationTask.unregisterService or mVisibilityTimer.start
 * 3. onTargetDeleted
 * <p>
 * <p>
 * Client:
 * <p>
 * Connect:
 * 1. startScan
 * 2. connect
 * 3. ConnectThread.start
 * <p>
 * Common:
 * <p>
 * Game:
 * 1. onSocketConnected or onSocketFailed
 * 2. ConnectedGame.start
 * 3. onRoomConnected
 * 4. onRoomUpdated
 * 5. reset
 */
public abstract class GameServiceSockets extends GameService
        implements OnSocketConnectedListener {

    private static final String TAG = "MultiplayerService";
    private Thread mSocketThread = null;

    private Target mTarget = null;

    @Override
    public void onDestroy() {
        if (mSocketThread != null) {
            mSocketThread.interrupt();
            mSocketThread = null;
        }
        super.onDestroy();
    }

    @CallSuper
    @Override
    public void connect(@NonNull Target target) {
        checkNotNull(target);
        checkState(mSocketThread == null);

        final SocketDecorator.Builder builder = mFactory.getSocketBuilder(target);
        mSocketThread = new ConnectThread(this, builder);
        mSocketThread.start();

        super.connect(target);
    }

    @Override
    public void onSocketConnected(@NonNull SocketDecorator socket) {
        checkNotNull(socket);
        checkNotNull(mSocketThread);

        Log.d(TAG, "onSocketConnected");

        mSocketThread = new ConnectedThread(this, this, socket);
        mSocketThread.start();
    }

    @CallSuper
    @Override
    public void onSocketFailed(@NonNull Exception exception) {
        checkNotNull(exception);
        checkNotNull(mSocketThread);

        Log.e(TAG, "onSocketFailed: " + exception.getMessage());
        Crashlytics.logException(exception);

        mSocketThread = null;
        sendMsg(exception);
        setConnectState(GameState.STATE_NONE);
    }

    @CallSuper
    void onServerSocketCreated(@NonNull ServerSocketDecorator serverSocket) {
        checkNotNull(serverSocket);

        Log.d(TAG, "onServerSocketCreated");
    }

    private void onServerSocketFailed(@NonNull Exception exception) {
        checkNotNull(exception);

        Log.e(TAG, "onServerSocketFailed: " + exception.getMessage());
    }

    @Override
    public void createTarget() {
        checkState(mSocketThread == null);

        try {
            final ServerSocketDecorator serverSocket = mFactory.getServerSocket();

            mSocketThread = new AcceptThread(this, serverSocket);
            mSocketThread.start();

            super.createTarget();

            onServerSocketCreated(serverSocket);
        } catch (IOException e) {
            onServerSocketFailed(e);
        }
    }

    @Override
    public void newGame() {
        final Room room = getRoom();

        checkNotNull(room);

        room.newGame();
        updateRoomRemotely(room);
    }

    @Override
    protected void updateRoomLocally(@NonNull Room room) {
        checkNotNull(room);

        if (room.getState() == Room.STATE_STARTED) {
            sendMsg(room);
        } else if (!room.getUser1().equals(getUser()) && room.getUser2() == null) {
            room.setState(Room.STATE_STARTED);
            room.setUser2(getUser());
            updateRoomRemotely(room);
        }
    }

    @Override
    protected void updateRoomRemotely(@NonNull Room room) {
        checkNotNull(room);
        checkState(mSocketThread instanceof ConnectedThread);

        final ConnectedThread connectedThread = (ConnectedThread) mSocketThread;
        connectedThread.write(room);
    }

    @CallSuper
    @Override
    protected void startGame(@Nullable Room room) {
        // it is always null for sockets
        checkState(room == null);
        // null can happen only on the client side
        if (getRoom() != null) {
            updateRoomRemotely(getRoom());
        }
    }

    @CallSuper
    @Override
    public void reset() {
        checkNotNull(mSocketThread);

        mSocketThread.interrupt();
        mSocketThread = null;

        mTarget = null;

        super.reset();
    }

    @Nullable
    @Override
    public Target getTarget() {
        return mTarget;
    }

    @Override
    public void onTargetCreated(@NonNull Target target) {
        checkNotNull(target);

        mTarget = target;
        setRoom(Room.newRoom(getUser()));

        super.onTargetCreated(target);
    }

    @Override
    public void onTargetUpdated(@NonNull Target target) {
        checkNotNull(target);
        mTarget = target;

        super.onTargetUpdated(target);
    }

    @Override
    public void onTargetCreationFailed(@NonNull Exception exception) {
        checkNotNull(exception);
        checkNotNull(mSocketThread);

        mSocketThread.interrupt();
        mSocketThread = null;

        super.onTargetCreationFailed(exception);
    }

    @Override
    public void onTargetDeleted(@Nullable Exception exception) {
        checkNotNull(mSocketThread);
        checkNotNull(mTarget);
        checkNotNull(getRoom());

        mSocketThread.interrupt();
        mSocketThread = null;

        mTarget = null;
        setRoom(null);

        super.onTargetDeleted(exception);
    }
}