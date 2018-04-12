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

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;

import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecoratorNsd;
import by.klnvch.link5dots.multiplayer.utils.nsd.NsdHelper;
import by.klnvch.link5dots.multiplayer.utils.nsd.RegistrationTask;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class GameServiceNsd extends GameServiceSockets {

    private RegistrationTask mRegistrationTask;

    @Override
    public void onCreate() {
        super.onCreate();
        NsdHelper.init(this);
        mRegistrationTask = new RegistrationTask();
    }

    @Override
    public void onDestroy() {
        mRegistrationTask.unregisterService(null);
        NsdHelper.destroy();
        super.onDestroy();
    }

    @Override
    public void onServerSocketCreated(@NonNull ServerSocketDecorator serverSocket) {
        super.onServerSocketCreated(serverSocket);

        final ServerSocketDecoratorNsd socket = (ServerSocketDecoratorNsd) serverSocket;
        final int port = socket.getLocalPort();
        mRegistrationTask.registerService(port, this);
    }

    @Override
    public void deleteTarget() {
        super.deleteTarget();
        mRegistrationTask.unregisterService(this);
    }

    @Override
    public void reset() {
        mRegistrationTask.unregisterService(null);
        super.reset();
    }
}