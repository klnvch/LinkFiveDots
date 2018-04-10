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

package by.klnvch.link5dots.multiplayer.sockets;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerSocketDecoratorNsd extends ServerSocketDecorator<ServerSocket> {

    @MainThread
    public ServerSocketDecoratorNsd() throws IOException {
        super(create());
    }

    @NonNull
    private static ServerSocket create() throws IOException {
        return new ServerSocket(0);
    }

    @Override
    public SocketDecorator accept() throws IOException {
        return new SocketDecoratorNsd(mSocket.accept());
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    public int getLocalPort() {
        return mSocket.getLocalPort();
    }

    @Override
    public String toString() {
        return mSocket.toString();
    }
}