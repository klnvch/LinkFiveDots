/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.BuildConfig
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.error.ErrorScreenOnline
import by.klnvch.link5dots.ui.game.picker.FirebaseStatusViewModel
import by.klnvch.link5dots.ui.game.picker.PickerScreen
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

@Composable
fun ConnectionStatus(statusViewModel: FirebaseStatusViewModel) {
    if (BuildConfig.DEBUG) {
        val isConnected by statusViewModel.isConnected.collectAsState()
        val textId = if (isConnected) R.string.connected else R.string.disconnected
        val color = if (isConnected) Color.Green else Color.Red
        Text(
            modifier = Modifier
                .background(color)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(textId),
        )
    }
}

class OnlineGameActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[OfflineGameViewModel.KEY, OnlineGameViewModel::class.java]

        val statusViewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[FirebaseStatusViewModel.KEY, FirebaseStatusViewModel::class.java]

        setContent {
            GameContent(
                viewModel = viewModel,
                defaultTitle = R.string.menu_online_game,
                pickerScreen = {
                    Column {
                        ConnectionStatus(statusViewModel)
                        PickerScreen(viewModel)
                    }
                },
                errorScreen = { e, onDone -> ErrorScreenOnline(e, onDone) },
                onFinish = { finish() }
            )
        }
    }
}
