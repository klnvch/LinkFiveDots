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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.support.annotation.MainThread;

import java.io.IOException;
import java.util.UUID;

import by.klnvch.link5dots.multiplayer.utils.bluetooth.BtCredentials;

public class ServerSocketDecoratorBluetooth extends ServerSocketDecorator<BluetoothServerSocket> {

    @MainThread
    public ServerSocketDecoratorBluetooth() throws IOException {
        super(create());
    }

    private static BluetoothServerSocket create() throws IOException {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        final String name = BtCredentials.NAME_SECURE;
        final UUID uuid = BtCredentials.UUID_SECURE;
        return adapter.listenUsingRfcommWithServiceRecord(name, uuid);
    }

    @Override
    public SocketDecorator accept() throws IOException {
        return new SocketDecoratorBluetooth(mSocket.accept());
    }

    @Override
    public void close() throws IOException {
        mSocket.close();
    }

    @Override
    public String toString() {
        return mSocket.toString();
    }
}