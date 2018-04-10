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

package by.klnvch.link5dots.multiplayer.nsd;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import by.klnvch.link5dots.R;
import by.klnvch.link5dots.multiplayer.common.AbstractGameActivity;
import by.klnvch.link5dots.multiplayer.services.GameServiceNsd;

public class NsdGameActivity extends AbstractGameActivity {

    private static final String TAG = "NsdGameActivity";

    @NonNull
    @Override
    protected Intent getServiceIntent() {
        return new Intent(this, GameServiceNsd.class);
    }

    @Override
    protected boolean isValid() {
        // NoClassDefFoundError (@by.klnvch.link5dots.fragment_game_picker.NsdService:<init>:294) {main}
        try {
            Class.forName("android.net.nsd.NsdManager");
            return true;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    @Override
    protected int getDefaultTitle() {
        return R.string.menu_local_network;
    }

    @Override
    public void newGame() {
        mService.newGame();
    }
}
