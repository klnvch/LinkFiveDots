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

package by.klnvch.link5dots.ui.menu

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.ui.game.activities.offline.BotGameActivity
import by.klnvch.link5dots.ui.game.activities.offline.GameInfoActivity.Companion.launchGameInfoActivity
import by.klnvch.link5dots.ui.game.activities.offline.TwoPlayersGameActivity
import by.klnvch.link5dots.ui.game.activities.online.BluetoothGameActivity
import by.klnvch.link5dots.ui.game.activities.online.NsdGameActivity
import by.klnvch.link5dots.ui.game.activities.online.OnlineGameActivity
import by.klnvch.link5dots.ui.settings.SettingsViewModel
import by.klnvch.link5dots.ui.theme.AppTheme
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlin.reflect.KClass

class MenuActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var savedStateViewModelFactory: SavedStateViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val getVMFactory: () -> ViewModelProvider.Factory = remember { { viewModelFactory } }
            val settingsViewModel: SettingsViewModel = viewModel(factory = getVMFactory())
            val nightMode by settingsViewModel.nightMode.collectAsState()
            AppTheme(nightMode) {
                val getSSVMFactory: () -> SavedStateViewModelFactory = remember {
                    { savedStateViewModelFactory }
                }
                App(getVMFactory, getSSVMFactory) { dest -> navigate(dest) }
            }
        }
    }

    private fun navigate(destination: Screen) {
        when (destination) {
            Screen.BotGame -> start(BotGameActivity::class)
            Screen.MultiplayerTwo -> start(TwoPlayersGameActivity::class)
            Screen.MultiplayerBluetooth -> start(BluetoothGameActivity::class)
            Screen.MultiplayerNsd -> start(NsdGameActivity::class)
            Screen.MultiplayerOnline -> start(OnlineGameActivity::class)
            is Screen.GameInfo -> launchGameInfoActivity(destination.key)
            else -> {}
        }
    }

    private fun start(cls: KClass<*>) = startActivity(Intent(this, cls.java))
}
