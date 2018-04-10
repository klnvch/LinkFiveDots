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

import android.annotation.TargetApi;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class SocketDecoratorNsd extends SocketDecorator<Socket> {
    SocketDecoratorNsd(@NonNull java.net.Socket socket) {
        super(socket);
    }

    @NonNull
    @Override
    public InputStream getInputStream() throws IOException {
        return mSocket.getInputStream();
    }

    @NonNull
    @Override
    public OutputStream getOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    public static class NsdBuilder extends Builder {

        private final NsdServiceInfo mServiceInfo;

        public NsdBuilder(@NonNull NsdServiceInfo serviceInfo) {
            mServiceInfo = serviceInfo;
        }

        @NonNull
        @Override
        public SocketDecorator build() throws IOException {
            final InetAddress inetAddress = mServiceInfo.getHost();
            final int port = mServiceInfo.getPort();
            return new SocketDecoratorNsd(new java.net.Socket(inetAddress, port));
        }
    }
}