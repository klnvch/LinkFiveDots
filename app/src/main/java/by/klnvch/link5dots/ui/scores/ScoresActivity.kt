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

package by.klnvch.link5dots.ui.scores

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.ActivityScoresBinding
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.ui.scores.history.HistoryFragment
import by.klnvch.link5dots.ui.scores.scores.ScoresFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class ScoresActivity : DaggerAppCompatActivity(), OnTabSelectedListener {
    private lateinit var binding: ActivityScoresBinding

    private lateinit var viewModel: ScoresViewModel

    @Inject
    lateinit var viewModelFactory: SavedStateViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.scores_title)

        viewModel = ViewModelProvider(this, viewModelFactory)[ScoresViewModel::class.java]

        binding.viewPager.adapter = PagerAdapter(this)
        binding.viewPager.currentItem = viewModel.getCurrentItem()
        binding.tabLayout.addOnTabSelectedListener(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                ScoresTabPosition.SCORES -> getString(R.string.scores_title)
                ScoresTabPosition.HISTORY -> getString(R.string.history)
                else -> throw IllegalStateException()
            }
        }.attach()
    }

    override fun onDestroy() {
        binding.tabLayout.removeOnTabSelectedListener(this)
        super.onDestroy()
    }

    private inner class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                ScoresTabPosition.SCORES -> ScoresFragment()
                ScoresTabPosition.HISTORY -> HistoryFragment()
                else -> throw IllegalStateException()
            }
        }

        override fun getItemCount(): Int = 2
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewModel.setCurrentItem(tab.position)
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {}

    override fun onTabReselected(tab: TabLayout.Tab) {}
}
