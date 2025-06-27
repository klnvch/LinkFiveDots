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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.MenuTextButton

@Composable
fun MultiplayerMenuScreen(onNavigate: (Screen) -> Unit) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> MultiplayerMenuScreenPortrait(onNavigate)
        else -> MultiplayerMenuScreenLandscape(onNavigate)
    }
}

@Composable
private fun MultiplayerMenuScreenPortrait(onNavigate: (Screen) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ButtonColumn1(onNavigate)
        ButtonColumn2(onNavigate)
    }
}

@Composable
private fun MultiplayerMenuScreenLandscape(onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ButtonColumn1(onNavigate)
        ButtonColumn2(onNavigate)
    }
}

@Composable
private fun ButtonColumn1(onNavigate: (Screen) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(Screen.MultiplayerTwo) },
            iconId = R.drawable.ic_people_48dp,
            textId = R.string.menu_two_players,
        )
        MenuTextButton(
            onClick = { onNavigate(Screen.MultiplayerBluetooth) },
            iconId = R.drawable.ic_bluetooth_48dp,
            textId = R.string.bluetooth,
        )
    }
}

@Composable
private fun ButtonColumn2(onNavigate: (Screen) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(Screen.MultiplayerNsd) },
            iconId = R.drawable.ic_router_48dp,
            textId = R.string.menu_local_network,
        )
        MenuTextButton(
            onClick = { onNavigate(Screen.MultiplayerOnline) },
            iconId = R.drawable.ic_public_48dp,
            textId = R.string.menu_online_game,
        )
    }
}
