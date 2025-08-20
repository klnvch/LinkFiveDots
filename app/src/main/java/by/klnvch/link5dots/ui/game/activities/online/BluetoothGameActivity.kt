/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.ui.game.activities.online

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.error.ErrorScreenBluetooth
import by.klnvch.link5dots.ui.game.picker.BluetoothPickerScreen
import by.klnvch.link5dots.ui.game.picker.VisibilityViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class BluetoothGameActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val gameViewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[OfflineGameViewModel.KEY, OnlineGameViewModel::class.java]

        val visibilityViewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[VisibilityViewModel.KEY, VisibilityViewModel::class.java]

        setContent {
            GameContent(
                viewModel = gameViewModel,
                defaultTitle = R.string.bluetooth,
                pickerScreen = { BluetoothPickerScreen(gameViewModel, visibilityViewModel) },
                errorScreen = { e, onDone -> ErrorScreenBluetooth(e, onDone) },
                onFinish = { finish() }
            )
        }
    }
}
