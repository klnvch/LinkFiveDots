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

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.ActivityScoresBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class ScoresActivity : DaggerAppCompatActivity() {
    private lateinit var binding: ActivityScoresBinding

    private val mDisposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.scores_title)

        binding.viewPager.adapter = PagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                TAB_SCORES -> getString(R.string.scores_title)
                TAB_HISTORY -> getString(R.string.history)
                else -> {
                    throw IllegalStateException()
                }
            }
        }.attach()
    }

    override fun onStart() {
        super.onStart()

        mDisposables.add(Observable.fromCallable { getCurrentItem() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { binding.viewPager.currentItem = it })
    }

    override fun onStop() {
        super.onStop()

        getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putInt(CURRENT_TAB_KEY, binding.viewPager.currentItem)
            .apply()
        mDisposables.clear()
    }

    private fun getCurrentItem(): Int {
        return getPreferences(Context.MODE_PRIVATE).getInt(CURRENT_TAB_KEY, 0)
    }

    private inner class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun createFragment(i: Int): Fragment {
            when (i) {
                TAB_SCORES -> return ScoresFragment()
                TAB_HISTORY -> return HistoryFragment()
            }
            throw IllegalStateException()
        }

        override fun getItemCount(): Int = 2
    }

    companion object {
        private const val CURRENT_TAB_KEY = "currentItem"
        private const val TAB_SCORES = 0
        private const val TAB_HISTORY = 1
    }
}
