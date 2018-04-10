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

package by.klnvch.link5dots.multiplayer.bt.tasks;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

import by.klnvch.link5dots.multiplayer.bt.BtCredentials;
import by.klnvch.link5dots.multiplayer.common.interfaces.OnTargetCreatedListener;
import by.klnvch.link5dots.multiplayer.common.interfaces.OnTargetDeletedListener;
import by.klnvch.link5dots.multiplayer.targets.TargetBluetoothLocal;

import static com.google.common.base.Preconditions.checkNotNull;

public class VisibilityTimer {

    private static final String TIME_FORMAT = "%d:%02d";

    private CountDownTimer mCountDownTimer = null;

    public VisibilityTimer() {
    }

    @NonNull
    private static String getTime(long time) {
        final long millisUntilFinished = time / 1000;
        final int min = (int) (millisUntilFinished / 60);
        final int sec = (int) (millisUntilFinished % 60);
        return String.format(Locale.getDefault(), TIME_FORMAT, min, sec);
    }

    public void start(@NonNull OnTargetCreatedListener createdListener,
                      @NonNull OnTargetDeletedListener deletedListener) {
        checkNotNull(createdListener);
        checkNotNull(deletedListener);

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

        createdListener.onTargetCreated(new TargetBluetoothLocal(
                getTime(BtCredentials.DISCOVERABLE_DURATION_MILLISECONDS)));

        mCountDownTimer = new CountDownTimer(BtCredentials.DISCOVERABLE_DURATION_MILLISECONDS,
                1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String time = getTime(millisUntilFinished);
                createdListener.onTargetUpdated(new TargetBluetoothLocal(time));
            }

            @Override
            public void onFinish() {
                deletedListener.onTargetDeleted(null);
                mCountDownTimer = null;
            }
        }.start();
    }

    // TODO: it is not real stop
    public void stop(@Nullable OnTargetDeletedListener deletedListener) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

        if (deletedListener != null) {
            deletedListener.onTargetDeleted(null);
        }
    }
}