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

package by.klnvch.link5dots.ui.game

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import by.klnvch.link5dots.GameFragment
import by.klnvch.link5dots.GameFragment.OnGameListener
import by.klnvch.link5dots.R
import by.klnvch.link5dots.models.Dot
import by.klnvch.link5dots.models.User
import by.klnvch.link5dots.ui.game.create.NewGameDialog
import by.klnvch.link5dots.ui.game.end.EndGameDialog
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class OfflineGameActivity : DaggerAppCompatActivity(), OnGameListener {
    private lateinit var mGameFragment: GameFragment

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: OfflineGameViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        mGameFragment = supportFragmentManager.findFragmentById(R.id.fragment) as GameFragment

        viewModel.room.observe(this) {
            if (it != null) {
                mGameFragment.update(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_game_offline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.menu_undo -> {
                viewModel.undoLastMove()
                true
            }
            R.id.menu_new_game -> {
                NewGameDialog().show(supportFragmentManager, NewGameDialog.TAG)
                true
            }
            R.id.menu_search -> onSearchRequested()
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSearchRequested(): Boolean {
        mGameFragment.focus()
        return true
    }

    override fun getUser(): User? {
        return null
    }

    override fun onMoveDone(dot: Dot) {
        viewModel.addDot(dot)
    }

    override fun onGameFinished() {
        EndGameDialog().show(supportFragmentManager, EndGameDialog.TAG)
    }
}
