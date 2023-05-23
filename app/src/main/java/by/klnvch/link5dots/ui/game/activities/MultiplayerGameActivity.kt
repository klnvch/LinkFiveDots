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
package by.klnvch.link5dots.ui.game.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.game.GameFragment
import by.klnvch.link5dots.ui.game.OfflineGameViewModel
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.error.MultiplayerErrorFragment
import by.klnvch.link5dots.ui.game.picker.PickerFragment
import kotlinx.coroutines.launch

abstract class MultiplayerGameActivity : GameActivity() {
    abstract val defaultTitle: Int

    override lateinit var viewModel: OnlineGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        )[OfflineGameViewModel.KEY, OnlineGameViewModel::class.java]

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, PickerFragment())
                .commit()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiTitleState.collect { setTitle(it) }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect {
                    when (it) {
                        is GameScreen -> {
                            supportFragmentManager
                                .beginTransaction()
                                .add(R.id.fragment, GameFragment())
                                .addToBackStack(null)
                                .commit()
                        }

                        is InitError -> {
                            supportFragmentManager
                                .beginTransaction()
                                .add(R.id.fragment, MultiplayerErrorFragment())
                                .commit()
                        }

                        is PickerScreen -> {
                            //supportFragmentManager
                            //    .beginTransaction()
                            //    .add(R.id.fragment, PickerFragment())
                            //    .commit()
                        }

                        is ConnectError -> {
                            val msg = getString(R.string.connecting_error_message, it.dst)
                            AlertDialog.Builder(this@MultiplayerGameActivity)
                                .setMessage(msg)
                                .setPositiveButton(R.string.okay, null)
                                .show()
                        }
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) { disconnectGuard(false) }
    }

    override fun onCreateOptionsMenu(menu: Menu) = true

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                disconnectGuard(true)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setTitle(titleId: Int) =
        if (titleId != 0) super.setTitle(titleId)
        else super.setTitle(defaultTitle)

    private fun disconnectGuard(isFullExit: Boolean) {
        if (supportFragmentManager.backStackEntryCount > 0 && viewModel.isConnected()) {
            val disconnectViewState = viewModel.disconnectViewState
            val msg = getString(R.string.is_disconnect_question, disconnectViewState.name)
            AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton(R.string.yes) { _, _ -> disconnectFinal(isFullExit) }
                .setNegativeButton(R.string.no, null)
                .show()
        } else {
            disconnectFinal(isFullExit)
        }
    }

    private fun disconnectFinal(isFullExit: Boolean) {
        viewModel.cleanUp()
        if (supportFragmentManager.backStackEntryCount > 0 && !isFullExit) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}