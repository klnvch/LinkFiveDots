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

package by.klnvch.link5dots

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import by.klnvch.link5dots.GameFragment.OnGameListener
import by.klnvch.link5dots.dialogs.NewGameDialog
import by.klnvch.link5dots.dialogs.NewGameDialog.OnSeedNewGameListener
import by.klnvch.link5dots.domain.repositories.Analytics
import by.klnvch.link5dots.domain.repositories.RoomDao
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.models.User
import by.klnvch.link5dots.utils.RoomUtils
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), OnGameListener, OnSeedNewGameListener {
    protected lateinit var mGameFragment: GameFragment
    protected var room: Room? = null

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var roomDao: RoomDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        mGameFragment = supportFragmentManager.findFragmentById(R.id.fragment) as GameFragment

        lifecycleScope.launch {
            val room = roomDao.getRecentRoomByType(getRoomType()).firstOrNull()
            val userName = settings.getUserName().first()
            val updatedRoom = if (room != null) {
                if (room.user1 != null) room.user1.name = userName
                room
            } else {
                val user = User.newUser(userName)
                createRoom(user)
            }
            withContext(Dispatchers.Main) {
                updateRoom(updatedRoom)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_game_offline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.menu_undo -> {
                analytics.logEvent(Analytics.EVENT_UNDO_MOVE)
                undoLastMove()
                return true
            }
            R.id.menu_new_game -> {
                analytics.logEvent(Analytics.EVENT_NEW_GAME)
                newGame()
                return true
            }
            R.id.menu_search -> {
                return onSearchRequested()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSearchRequested(): Boolean {
        analytics.logEvent(Analytics.EVENT_SEARCH)
        searchLastMove()
        return true
    }

    protected abstract fun undoLastMove()

    protected fun newGame() {
        val room = this.room
        if (room != null) {
            mGameFragment.update(RoomUtils.newGame(room, null))
            NewGameDialog().show(supportFragmentManager, NewGameDialog.TAG)
        }
    }

    override fun onSeedNewGame(seed: Long?) {
        val room = this.room
        if (room != null) {
            analytics.logEvent(Analytics.EVENT_GENERATE_GAME)
            mGameFragment.update(RoomUtils.newGame(room, seed))
        }
    }

    private fun searchLastMove() {
        mGameFragment.focus()
    }

    private fun updateRoom(room: Room) {
        mGameFragment.update(room.also { this.room = it })
    }

    protected abstract fun createRoom(host: User?): Room

    protected abstract fun getRoomType(): Int
}
