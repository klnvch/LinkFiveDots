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
import androidx.navigation.fragment.findNavController
import by.klnvch.link5dots.R
import by.klnvch.link5dots.databinding.FragmentMultiplayerMenuBinding
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToGameActivityBluetooth
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToGameActivityNsd
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToGameActivityOnline
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToTwoPlayersActivity
import dagger.android.support.DaggerFragment

class MultiplayerMenuFragment : DaggerFragment(), OnMultiplayerMenuListener {
    private lateinit var binding: FragmentMultiplayerMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiplayerMenuBinding.inflate(inflater, container, false)
        binding.listener = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.menu_multi_player)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onTwoPlayersGameClicked() =
        findNavController().navigate(actionMultiplayerMenuFragmentToTwoPlayersActivity())

    override fun onBluetoothGameClicked() =
        findNavController().navigate(actionMultiplayerMenuFragmentToGameActivityBluetooth())

    override fun onNsdGameClicked() =
        findNavController().navigate(actionMultiplayerMenuFragmentToGameActivityNsd())

    override fun onOnlineGameClicked() =
        findNavController().navigate(actionMultiplayerMenuFragmentToGameActivityOnline())
}
