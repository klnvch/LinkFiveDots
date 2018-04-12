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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.utils.GameState;
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper;
import by.klnvch.link5dots.multiplayer.utils.bluetooth.VisibilityTimer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class GameServiceBluetooth extends GameServiceSockets {

    private VisibilityTimer mVisibilityTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        mVisibilityTimer = new VisibilityTimer();
    }

    @Override
    public void onDestroy() {
        mVisibilityTimer.stop(null);
        super.onDestroy();
    }

    @Override
    public void connect(@NonNull Target target) {
        checkNotNull(target);
        /*
         * Creating new connections to remote Bluetooth devices should not be attempted while
         * device discovery is in progress. Device discovery is a heavyweight procedure on the
         * Bluetooth adapter and will significantly slow a device connection. Use cancelDiscovery()
         * to cancel an ongoing discovery. Discovery is not managed by the Activity, but is run as
         * a system service, so an application should always call cancelDiscovery() even if it did
         * not directly request a discovery, just to be sure.
         */
        BluetoothHelper.cancelDiscovery();

        super.connect(target);
    }

    @Override
    public void onSocketFailed(@NonNull Exception exception) {
        checkNotNull(exception);

        // resume scanning
        if (mScanner.isScanning()) {
            BluetoothHelper.startDiscovery();
        }

        super.onSocketFailed(exception);
    }

    @Override
    protected void startGame(@Nullable Room room) {
        mVisibilityTimer.stop(null);
        super.startGame(room);
    }

    @Override
    public void onServerSocketCreated(@NonNull ServerSocketDecorator serverSocket) {
        super.onServerSocketCreated(serverSocket);
        mVisibilityTimer.start(this, this);
    }

    @Override
    public void deleteTarget() {
        super.deleteTarget();
        mVisibilityTimer.stop(this);
    }

    @Override
    public void reset() {
        mVisibilityTimer.stop(null);

        super.reset();
    }

    @Override
    public void onScanStopped(@Nullable Exception e) {
        if (e != null) {
            super.onScanStopped(e);
        } else {
            setScanState(GameState.STATE_SCAN_DONE);
        }
    }
}