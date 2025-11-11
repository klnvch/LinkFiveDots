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
package by.klnvch.link5dots.ui.game.activities.offline

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.TopBarTitle
import by.klnvch.link5dots.ui.game.viewmodels.BaseGameViewModel
import by.klnvch.link5dots.ui.game.viewmodels.InfoGameViewModel
import by.klnvch.link5dots.ui.settings.SettingsViewModel
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class GameInfoActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val gameViewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[BaseGameViewModel.KEY, InfoGameViewModel::class.java]

        setContent {
            val getVMFactory: () -> ViewModelProvider.Factory = remember { { viewModelFactory } }
            val settingsViewModel: SettingsViewModel = viewModel(factory = getVMFactory())
            val nightMode by settingsViewModel.nightMode.collectAsState()
            LaunchedEffect(true) {
                gameViewModel.setKey(getKey())
            }
            GameContent(
                actions = gameViewModel,
                nightMode = nightMode,
                title = { TopBarTitle(R.string.application_info_label) },
                navigateUp = { finish() },
            )
        }
    }

    private fun getKey(): String {
        val key = intent.getStringExtra(KEY)
        if (key != null) return key
        else throw IllegalArgumentException()
    }

    companion object {
        private const val KEY = "key"

        fun Activity.launchGameInfoActivity(key: String) =
            startActivity(Intent(this, GameInfoActivity::class.java).putExtra(KEY, key))
    }
}
