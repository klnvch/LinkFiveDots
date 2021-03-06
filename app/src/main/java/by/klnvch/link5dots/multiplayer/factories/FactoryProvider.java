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

import androidx.annotation.NonNull;
import by.klnvch.link5dots.multiplayer.activities.GameActivityBluetooth;
import by.klnvch.link5dots.multiplayer.activities.GameActivityNsd;
import by.klnvch.link5dots.multiplayer.activities.GameActivityOnline;
import by.klnvch.link5dots.multiplayer.services.GameServiceBluetooth;
import by.klnvch.link5dots.multiplayer.services.GameServiceNsd;
import by.klnvch.link5dots.multiplayer.services.GameServiceOnline;

public class FactoryProvider {
    @NonNull
    public static FactoryActivityInterface getActivityFactory(@NonNull Class c) {
        if (c.equals(GameActivityOnline.class)) {
            return new FactoryOnline();
        } else if (c.equals(GameActivityNsd.class)) {
            return new FactoryNsd();
        } else if (c.equals(GameActivityBluetooth.class)) {
            return new FactoryBluetooth();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @NonNull
    public static FactoryServiceInterface getServiceFactory(@NonNull Class c) {
        if (c.equals(GameServiceOnline.class)) {
            return new FactoryOnline();
        } else if (c.equals(GameServiceNsd.class)) {
            return new FactoryNsd();
        } else if (c.equals(GameServiceBluetooth.class)) {
            return new FactoryBluetooth();
        } else {
            throw new IllegalArgumentException();
        }
    }
}