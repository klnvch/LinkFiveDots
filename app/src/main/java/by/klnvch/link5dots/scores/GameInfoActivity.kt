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
import by.klnvch.link5dots.databinding.ActivityGameBinding
import by.klnvch.link5dots.models.Dot
import by.klnvch.link5dots.models.User
import by.klnvch.link5dots.utils.IntentExt.getRoom
import dagger.android.support.DaggerAppCompatActivity

class GameInfoActivity : DaggerAppCompatActivity(), GameFragment.OnGameListener {
    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.application_info_label)
    }

    override fun onStart() {
        super.onStart()
        val room = intent.getRoom()
        if (room != null) {
            (binding.fragment.getFragment() as GameFragment).update(room)
        } else {
            binding.errorMessage.visibility = View.VISIBLE
        }
    }

    override fun onMoveDone(dot: Dot) {
    }

    override fun onGameFinished() {
    }

    override fun getUser(): User? {
        return intent.getRoom()?.user1
    }
}
