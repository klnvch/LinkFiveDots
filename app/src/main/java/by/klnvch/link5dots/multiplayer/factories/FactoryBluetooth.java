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

package by.klnvch.link5dots.multiplayer.factories;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;

import by.klnvch.link5dots.multiplayer.adapters.PickerAdapterBluetooth;
import by.klnvch.link5dots.multiplayer.adapters.TargetAdapterInterface;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecoratorBluetooth;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecoratorBluetooth;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.multiplayer.targets.TargetBluetooth;

public class FactoryBluetooth implements FactoryInterface {
    @NonNull
    @Override
    public TargetAdapterInterface getAdapter(@NonNull Context context) {
        return new PickerAdapterBluetooth(context);
    }

    @NonNull
    @Override
    public SocketDecorator.Builder getSocketBuilder(@NonNull Target target) {
        final TargetBluetooth targetBluetooth = (TargetBluetooth) target;
        return new SocketDecoratorBluetooth.BtBuilder(targetBluetooth.getTarget());
    }

    @NonNull
    @Override
    public ServerSocketDecorator getServerSocket() throws IOException {
        return new ServerSocketDecoratorBluetooth();
    }
}