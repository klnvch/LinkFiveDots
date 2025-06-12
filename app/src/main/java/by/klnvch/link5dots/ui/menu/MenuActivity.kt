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
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.ui.HowToActivity
import by.klnvch.link5dots.ui.InfoActivity
import by.klnvch.link5dots.ui.game.activities.BluetoothGameActivity
import by.klnvch.link5dots.ui.game.activities.BotGameActivity
import by.klnvch.link5dots.ui.game.activities.NsdGameActivity
import by.klnvch.link5dots.ui.game.activities.OnlineGameActivity
import by.klnvch.link5dots.ui.game.activities.TwoPlayersGameActivity
import by.klnvch.link5dots.ui.scores.ScoresActivity
import by.klnvch.link5dots.ui.settings.SettingsActivity
import by.klnvch.link5dots.ui.theme.AppTheme
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import kotlin.reflect.KClass

class MenuActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this, viewModelFactory)[MainMenuViewModel::class.java]

        setContent {
            AppTheme {
                App(viewModel) { dest -> navigate(dest) }
            }
        }
    }

    private fun navigate(destination: Screen) {
        when (destination) {
            Screen.UserNameDialog -> UsernameDialog().show(
                supportFragmentManager,
                UsernameDialog.TAG
            )

            Screen.BotGame -> start(BotGameActivity::class)
            Screen.Scores -> start(ScoresActivity::class)
            Screen.Settings -> start(SettingsActivity::class)
            Screen.Info -> start(InfoActivity::class)
            Screen.Help -> start(HowToActivity::class)
            Screen.MultiplayerTwo -> start(TwoPlayersGameActivity::class)
            Screen.MultiplayerBluetooth -> start(BluetoothGameActivity::class)
            Screen.MultiplayerNsd -> start(NsdGameActivity::class)
            Screen.MultiplayerOnline -> start(OnlineGameActivity::class)
            else -> {}
        }
    }

    private fun start(cls: KClass<*>) = startActivity(Intent(this, cls.java))
}
