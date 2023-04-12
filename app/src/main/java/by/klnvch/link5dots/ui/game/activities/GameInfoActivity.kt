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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.Fragment
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.IRoom
import by.klnvch.link5dots.domain.usecases.room.GetRoomUseCase

class GameInfoActivity : OfflineGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.application_info_label)
    }

    override fun onCreateOptionsMenu(menu: Menu) = true

    override fun getParam(): GetRoomUseCase.RoomParam {
        val key = intent.getStringExtra(KEY)
        if (key != null) return GetRoomUseCase.RoomByKey(key)
        else throw IllegalArgumentException()
    }

    override fun onGameEnd() = Unit

    companion object {
        private const val KEY = "key"
        fun Fragment.launchGameInfoActivity(room: IRoom) {
            startActivity(Intent(context, GameInfoActivity::class.java).putExtra(KEY, room.key))
        }
    }
}
