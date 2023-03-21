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

package by.klnvch.link5dots.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.klnvch.link5dots.databinding.FragmentMainMenuBinding
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToHowToActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToInfoActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToMainActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToMultiplayerMenuFragment
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToScoresActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToSettingsActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToUsernameDialog
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainMenuFragment : DaggerFragment(), OnMainMenuActionListener {
    private lateinit var viewModel: MainMenuViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentMainMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[MainMenuViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.map { it.userName }.collect { binding.userName = it }
            }
        }

        binding.listener = this
    }

    override fun onUserNameClicked() =
        findNavController().navigate(actionMainMenuFragmentToUsernameDialog())

    override fun onSinglePlayerClicked() =
        findNavController().navigate(actionMainMenuFragmentToMainActivity())

    override fun onMultiplayerPlayerClicked() =
        findNavController().navigate(actionMainMenuFragmentToMultiplayerMenuFragment())

    override fun onScoresClicked() =
        findNavController().navigate(actionMainMenuFragmentToScoresActivity())

    override fun onSettingsClicked() =
        findNavController().navigate(actionMainMenuFragmentToSettingsActivity())

    override fun onInfoClicked() =
        findNavController().navigate(actionMainMenuFragmentToInfoActivity())

    override fun onHelpClicked() =
        findNavController().navigate(actionMainMenuFragmentToHowToActivity())
}
