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

import android.bluetooth.BluetoothServerSocket;

import java.io.IOException;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper;

public class ServerSocketDecoratorBluetooth extends ServerSocketDecorator<BluetoothServerSocket> {

    @MainThread
    public ServerSocketDecoratorBluetooth() throws IOException {
        super(BluetoothHelper.INSTANCE.createServerSocket());
    }

    @Override
    public SocketDecorator accept() throws IOException {
        return new SocketDecoratorBluetooth(mSocket.accept());
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    @NonNull
    @Override
    public String toString() {
        return mSocket.toString();
    }
}