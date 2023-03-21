/*
 * MIT License
 *
 * Copyright (c) 2023 klnvch
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

package by.klnvch.link5dots.multiplayer.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import by.klnvch.link5dots.multiplayer.utils.bluetooth.BluetoothHelper;

public class GameActivityBluetooth extends GameActivity {
    private static final int RC_ENABLE_BLUETOOTH = 3;

    @Override
    public void newGame() {
        mGameFragment.reset();
        mService.newGame();
    }

    @Override
    protected boolean isValidFomMainMenuMoved() {
        if (BluetoothHelper.INSTANCE.isSupported()) {
            if (!BluetoothHelper.INSTANCE.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), RC_ENABLE_BLUETOOTH);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_ENABLE_BLUETOOTH) {
            if (resultCode != RESULT_OK) {
                setResult(RESULT_CANCELED);
                showErrorDialog();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
