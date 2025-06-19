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

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.R
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.ui.game.activities.BluetoothGameActivity
import by.klnvch.link5dots.ui.game.activities.BotGameActivity
import by.klnvch.link5dots.ui.game.activities.GameInfoActivity.Companion.launchGameInfoActivity
import by.klnvch.link5dots.ui.game.activities.NsdGameActivity
import by.klnvch.link5dots.ui.game.activities.OnlineGameActivity
import by.klnvch.link5dots.ui.game.activities.TwoPlayersGameActivity
import by.klnvch.link5dots.ui.scores.ScoresActivity
import by.klnvch.link5dots.ui.settings.SettingsActivity
import by.klnvch.link5dots.ui.theme.AppTheme
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

        val viewModel = ViewModelProvider(this, viewModelFactory)[MainMenuViewModel::class.java]

        setContent {
            AppTheme {
                val getVmFactory: () -> SavedStateViewModelFactory = remember {
                    { savedStateViewModelFactory }
                }
                App(getVmFactory, viewModel) { dest -> navigate(dest) }
            }
        }
    }

    private fun navigate(destination: Screen) {
        when (destination) {
            Screen.BotGame -> start(BotGameActivity::class)
            Screen.Scores -> start(ScoresActivity::class)
            Screen.Settings -> start(SettingsActivity::class)
            Screen.MultiplayerTwo -> start(TwoPlayersGameActivity::class)
            Screen.MultiplayerBluetooth -> start(BluetoothGameActivity::class)
            Screen.MultiplayerNsd -> start(NsdGameActivity::class)
            Screen.MultiplayerOnline -> start(OnlineGameActivity::class)
            is Screen.GameInfo -> launchGameInfoActivity(destination.key)
            Screen.SourceCode -> {
                val intent = Intent(Intent.ACTION_VIEW, GITHUB_LINK.toUri())
                launchIntent(intent, GITHUB_LINK)
            }

            Screen.RateApp -> {
                val intent = Intent(Intent.ACTION_VIEW, ANDROID_APP_LINK.toUri())
                launchIntent(intent, WEB_PAGE_LINK)
            }

            Screen.ShareApp -> {
                val intent = Intent(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_TEXT, WEB_PAGE_LINK)
                    .setType("text/plain")
                launchIntent(intent, WEB_PAGE_LINK)
            }

            Screen.Feedback -> {
                val intent = Intent.createChooser(
                    Intent(Intent.ACTION_SENDTO)
                        .setDataAndType(URI_MAIL_DATA.toUri(), "message/rfc822")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    getString(R.string.connection_error_message)
                )
                launchIntent(intent, MAIL)
            }

            else -> {}
        }
    }

    private fun start(cls: KClass<*>) = startActivity(Intent(this, cls.java))

    private fun launchIntent(intent: Intent, errorMsg: String) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(e)

            AlertDialog.Builder(this)
                .setMessage(errorMsg)
                .setPositiveButton(R.string.okay, null)
                .setNeutralButton(R.string.copy_text) { _, _ -> copyToClipboard(errorMsg) }
                .show()
        }
    }

    private fun copyToClipboard(msg: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(msg, msg)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.toast_text_copied, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val WEB_PAGE_LINK =
            "https://play.google.com/store/apps/details?id=by.klnvch.link5dots"
        private const val ANDROID_APP_LINK = "market://details?id=by.klnvch.link5dots"
        private const val GITHUB_LINK = "https://github.com/klnvch/LinkFiveDots"
        private const val MAIL = "link5dots@gmail.com"
        private const val URI_MAIL_DATA = "mailto:$MAIL"
    }
}
