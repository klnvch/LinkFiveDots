/*
 * MIT License
 *
 * Copyright (c) 2017 klnvch
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

package by.klnvch.link5dots.scores

import android.os.Bundle
import android.view.View
import by.klnvch.link5dots.GameFragment
import by.klnvch.link5dots.R
import by.klnvch.link5dots.models.Dot
import by.klnvch.link5dots.models.Room
import by.klnvch.link5dots.models.User
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*

class GameInfoActivity : DaggerAppCompatActivity(), GameFragment.OnGameListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setTitle(R.string.application_info_label)
    }

    override fun onStart() {
        super.onStart()
        val room = intent.getSerializableExtra("room")
        if (room != null) {
            (fragment as GameFragment).update(room as Room)
        } else {
            errorMessage.visibility = View.VISIBLE
        }
    }

    override fun onMoveDone(dot: Dot) {
    }

    override fun onGameFinished() {
    }

    override fun getUser(): User? {
        val room = intent.getSerializableExtra("room")
        room?.let {
            return (room as Room).user1
        }
        return null
    }
}