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

import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.util.Random;

import by.klnvch.link5dots.domain.models.NetworkRoom;
import by.klnvch.link5dots.domain.models.NetworkUser;
import by.klnvch.link5dots.domain.models.RoomState;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.threads.AcceptThread;
import by.klnvch.link5dots.multiplayer.threads.ConnectThread;
import by.klnvch.link5dots.multiplayer.threads.ConnectedThread;
import by.klnvch.link5dots.multiplayer.threads.OnSocketConnectedListener;
import by.klnvch.link5dots.multiplayer.utils.GameState;
import by.klnvch.link5dots.utils.RoomUtils;

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

    private NetworkUser mUser;

    private static final String TAG = "MultiplayerService";
    private Thread mSocketThread = null;

    private Target mTarget = null;

    @Override
    public void onCreate() {
        super.onCreate();

        final String id = Long.toHexString(System.currentTimeMillis()) + '_' + Long.toHexString(new Random().nextLong());
        mUser = new NetworkUser(id, settings.getUserNameBlocking());
    }

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
        final SocketDecorator.Builder builder = mFactory.getSocketBuilder(target);
        mSocketThread = new ConnectThread(this, builder);
        mSocketThread.start();

        super.connect(target);
    }

    @Override
    public void onSocketConnected(@NonNull SocketDecorator socket) {
        Log.d(TAG, "onSocketConnected");

        mSocketThread = new ConnectedThread(this, this, socket);
        mSocketThread.start();
    }

    @CallSuper
    @Override
    public void onSocketFailed(@NonNull Exception exception) {
        Log.e(TAG, "onSocketFailed: " + exception.getMessage());
        FirebaseCrashlytics.getInstance().recordException(exception);

        mSocketThread = null;
        sendMsg(exception);
        setConnectState(GameState.ConnectState.NONE);
    }

    @CallSuper
    void onServerSocketCreated(@NonNull ServerSocketDecorator serverSocket) {
        Log.d(TAG, "onServerSocketCreated");
    }

    private void onServerSocketFailed(@NonNull Exception exception) {
        Log.e(TAG, "onServerSocketFailed: " + exception.getMessage());
    }

    @Override
    public void createTarget() {
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
        final NetworkRoom room = getRoom();
        RoomUtils.newGame(room, null);
        updateRoomRemotely(room);
    }

    @Override
    protected void updateRoomLocally(@NonNull NetworkRoom room) {
        final NetworkUser currentUser = getUser();
        if (room.getState() == RoomState.STARTED) {
            sendMsg(room);
        } else if (!currentUser.equals(room.getUser1()) && room.getUser2() == null) {
            updateRoomRemotely(room.copy(room.getKey(), room.getTimestamp(), room.getDots(), room.getUser1(), currentUser, room.getType(), RoomState.STARTED));
        }
    }

    @Override
    protected void updateRoomRemotely(@NonNull NetworkRoom room) {
        final ConnectedThread connectedThread = (ConnectedThread) mSocketThread;
        connectedThread.write(room);
    }

    @CallSuper
    @Override
    protected void startGame(@Nullable NetworkRoom room) {
        // null can happen only on the client side
        if (getRoom() != null) {
            updateRoomRemotely(getRoom());
        }
    }

    @CallSuper
    @Override
    public void reset() {
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
        mTarget = target;
        setRoom(RoomUtils.createMultiplayerGame(getUser(), mFactory.getRoomType()));

        super.onTargetCreated(target);
    }

    @Override
    public void onTargetCreationFailed(@NonNull Throwable e) {
        mSocketThread.interrupt();
        mSocketThread = null;

        super.onTargetCreationFailed(e);
    }

    @Override
    public void onTargetDeleted(@Nullable Exception exception) {
        mSocketThread.interrupt();
        mSocketThread = null;

        mTarget = null;
        setRoom(null);

        super.onTargetDeleted(exception);
    }

    @NonNull
    @Override
    public NetworkUser getUser() {
        return mUser;
    }
}
