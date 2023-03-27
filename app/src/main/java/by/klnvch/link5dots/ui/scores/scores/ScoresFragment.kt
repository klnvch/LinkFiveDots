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

package by.klnvch.link5dots.ui.scores.scores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import by.klnvch.link5dots.databinding.FragmentScoresBinding
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.ui.scores.ScoresViewModel
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScoresFragment : DaggerFragment() {
    private lateinit var binding: FragmentScoresBinding
    private lateinit var scoresAdapter: ScoresAdapter
    private lateinit var viewModel: ScoresViewModel

    @Inject
    lateinit var viewModelFactory: SavedStateViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScoresBinding.inflate(inflater, container, false)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[ScoresViewModel::class.java]

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scoresUiState.collect { onViewStateUpdated(it) }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStop() {
        scoresAdapter.stopListening()
        viewModel.signOut()
        super.onStop()
    }

    private fun onViewStateUpdated(viewState: ScoresViewState) {
        binding.viewState = viewState

        if (viewState.highScorePath != null) {
            scoresAdapter = ScoresAdapter.create(viewState.highScorePath)
            binding.recyclerView.adapter = scoresAdapter
        }

        if (viewState.firebaseState == FirebaseState.SIGNED_IN) {
            scoresAdapter.startListening()
        }
    }
}
