/*
 * MIT License
 *
 * Copyright (c) 2023-2025 klnvch
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

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.MenuTextButton
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToGameActivityBluetooth
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToGameActivityNsd
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToGameActivityOnline
import by.klnvch.link5dots.ui.menu.MultiplayerMenuFragmentDirections.Companion.actionMultiplayerMenuFragmentToTwoPlayersActivity
import by.klnvch.link5dots.ui.theme.AppTheme
import dagger.android.support.DaggerFragment

class MultiplayerMenuFragment : DaggerFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MultiplayerMenuScreen { dest -> findNavController().navigate(dest) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.menu_multi_player)
        super.onViewCreated(view, savedInstanceState)
    }
}

@Composable
fun MultiplayerMenuScreen(onNavigate: (NavDirections) -> Unit) {
    val configuration = LocalConfiguration.current
    AppTheme {
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> MultiplayerMenuScreenPortrait(onNavigate)
            else -> MultiplayerMenuScreenLandscape(onNavigate)
        }
    }
}

@Composable
fun MultiplayerMenuScreenPortrait(onNavigate: (NavDirections) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        ButtonColumn1(onNavigate)
        ButtonColumn2(onNavigate)
    }
}

@Composable
fun MultiplayerMenuScreenLandscape(onNavigate: (NavDirections) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row {
            ButtonColumn1(onNavigate)
            ButtonColumn2(onNavigate)
        }
    }
}

@Composable
fun ButtonColumn1(onNavigate: (NavDirections) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(actionMultiplayerMenuFragmentToTwoPlayersActivity()) },
            iconId = R.drawable.ic_people_48dp,
            textId = R.string.menu_two_players,
        )
        MenuTextButton(
            onClick = { onNavigate(actionMultiplayerMenuFragmentToGameActivityBluetooth()) },
            iconId = R.drawable.ic_bluetooth_48dp,
            textId = R.string.bluetooth,
        )
    }
}

@Composable
fun ButtonColumn2(onNavigate: (NavDirections) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(actionMultiplayerMenuFragmentToGameActivityNsd()) },
            iconId = R.drawable.ic_router_48dp,
            textId = R.string.menu_local_network,
        )
        MenuTextButton(
            onClick = { onNavigate(actionMultiplayerMenuFragmentToGameActivityOnline()) },
            iconId = R.drawable.ic_public_48dp,
            textId = R.string.menu_online_game,
        )
    }
}
