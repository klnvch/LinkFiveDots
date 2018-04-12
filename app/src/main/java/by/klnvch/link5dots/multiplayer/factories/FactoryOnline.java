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
import android.content.Intent;
import android.support.annotation.NonNull;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.activities.PickerFragment;
import by.klnvch.link5dots.multiplayer.adapters.PickerAdapterOnline;
import by.klnvch.link5dots.multiplayer.adapters.TargetAdapterInterface;
import by.klnvch.link5dots.multiplayer.services.GameServiceOnline;
import by.klnvch.link5dots.multiplayer.sockets.ServerSocketDecorator;
import by.klnvch.link5dots.multiplayer.sockets.SocketDecorator;
import by.klnvch.link5dots.multiplayer.targets.Target;
import by.klnvch.link5dots.utils.AvailabilityChecker;

public class FactoryOnline implements FactoryServiceInterface, FactoryActivityInterface {
    @NonNull
    @Override
    public TargetAdapterInterface getAdapter(@NonNull Context context) {
        return PickerAdapterOnline.createAdapter();
    }

    @NonNull
    @Override
    public SocketDecorator.Builder getSocketBuilder(@NonNull Target target) {
        throw new IllegalStateException();
    }

    @NonNull
    @Override
    public ServerSocketDecorator getServerSocket() {
        throw new IllegalStateException();
    }

    @NonNull
    @Override
    public Intent getServiceIntent(@NonNull Context context) {
        return new Intent(context, GameServiceOnline.class);
    }

    @Override
    public boolean isValid(@NonNull Context context) {
        return AvailabilityChecker.isGPSValid(context);
    }

    @Override
    public int getDefaultTitle() {
        return R.string.menu_online_game;
    }

    @NonNull
    @Override
    public PickerFragment getPickerFragment() {
        return new PickerFragment();
    }
}