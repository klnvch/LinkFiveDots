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

package by.klnvch.link5dots.multiplayer.threads;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import by.klnvch.link5dots.models.Room;
import by.klnvch.link5dots.multiplayer.common.interfaces.OnRoomConnectedListener;
import by.klnvch.link5dots.multiplayer.common.interfaces.OnRoomUpdatedListener;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;

/**
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";
    private static final int THREAD_ID = 3;

    private final SocketDecorator mSocket;
    private final WeakReference<OnRoomConnectedListener> mConnectedListener;
    private final WeakReference<OnRoomUpdatedListener> mUpdatedListener;

    private DataOutputStream mOutputStream;

    public ConnectedThread(@NonNull OnRoomConnectedListener connectedListener,
                           @NonNull OnRoomUpdatedListener updatedListener,
                           @NonNull SocketDecorator socket) {
        setName(TAG);

        this.mConnectedListener = new WeakReference<>(connectedListener);
        this.mUpdatedListener = new WeakReference<>(updatedListener);
        this.mSocket = socket;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: started");

        DataInputStream mInputStream = null;
        Exception exception = null;

        try {
            TrafficStats.setThreadStatsTag(THREAD_ID);
            mInputStream = new DataInputStream(mSocket.getInputStream());
            mOutputStream = new DataOutputStream(mSocket.getOutputStream());
        } catch (IOException e) {
            exception = e;
        }

        final OnRoomConnectedListener connectedListener = mConnectedListener.get();
        if (connectedListener != null) {
            if (exception == null) {
                connectedListener.onRoomConnected(null, null);
            } else {
                if (!isInterrupted()) {
                    connectedListener.onRoomConnected(null, exception);
                }
                return;
            }
        } else {
            Log.e(TAG, "run: connected listener is null");
            return;
        }

        // Keep listening to the InputStream while connected
        while (true) {
            Room room = null;

            // Read from the InputStream
            try {
                final String msg = mInputStream.readUTF();
                room = Room.fromJson(msg);
            } catch (Exception e) {
                exception = e;
            }

            // notify listener, and continue on success or quit on fail
            final boolean isNotified = notifyUpdatedListener(room, exception);
            if (exception != null || !isNotified) {
                break;
            }
        }

        Log.d(TAG, "run: finished");
    }

    /**
     * Write room the connected OutputStream.
     *
     * @param room The room to write
     */
    public void write(@NonNull Room room) {
        // NetworkOnMainThreadException - StrictMode$AndroidBlockGuardPolicy.onNetwork
        new WriterTask(this).execute(room);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of socket failed", e);
        }
    }

    private boolean notifyUpdatedListener(@Nullable Room room, @Nullable Exception exception) {
        final OnRoomUpdatedListener updatedListener = mUpdatedListener.get();
        if (updatedListener != null) {
            if (exception != null) {
                if (!isInterrupted()) {
                    updatedListener.onRoomUpdated(null, exception);
                }
            } else {
                updatedListener.onRoomUpdated(room, null);
            }
            return true;
        } else {
            Log.e(TAG, "notifyUpdatedListener: listener is null");
            return false;
        }
    }

    private static class WriterTask extends AsyncTask<Room, Void, Void> {
        private final WeakReference<ConnectedThread> mThreadRef;

        private WriterTask(@NonNull ConnectedThread thread) {
            this.mThreadRef = new WeakReference<>(thread);
        }

        @Override
        protected Void doInBackground(Room... params) {
            final ConnectedThread thread = mThreadRef.get();
            if (thread != null) {
                try {
                    final Room room = params[0];
                    final String json = room.toJson();

                    thread.mOutputStream.writeUTF(json);
                    thread.mOutputStream.flush();
                    thread.notifyUpdatedListener(room, null);
                } catch (IOException e) {
                    thread.notifyUpdatedListener(null, e);
                }
            } else {
                Log.e(TAG, "WriterTask: thread is null");
            }
            return null;
        }
    }
}