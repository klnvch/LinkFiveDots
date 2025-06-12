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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.MenuTextButton
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToHowToActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToInfoActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToMainActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToMultiplayerMenuFragment
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToScoresActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToSettingsActivity
import by.klnvch.link5dots.ui.menu.MainMenuFragmentDirections.Companion.actionMainMenuFragmentToUsernameDialog
import by.klnvch.link5dots.ui.theme.AppTheme
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class MainMenuFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val viewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[MainMenuViewModel::class.java]
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MainMenuScreen(viewModel) { dest -> findNavController().navigate(dest) }
            }
        }
    }
}

@Composable
fun MainMenuScreen(viewModel: MainMenuViewModel, onNavigate: (NavDirections) -> Unit) {
    val configuration = LocalConfiguration.current
    val uiState by viewModel.uiState.collectAsState()
    AppTheme {
        val userName = uiState.userName
        when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> MainMenuScreenPortrait(userName, onNavigate)
            else -> MainMenuScreenLandscape(userName, onNavigate)
        }
    }
}

@Composable
fun MainMenuScreenPortrait(userName: String, onNavigate: (NavDirections) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        GreetingText(
            onClick = { onNavigate(actionMainMenuFragmentToUsernameDialog()) },
            userName = userName,
            modifier = Modifier.widthIn(0.dp, 320.dp)
        )
        GameButtonColumn(onNavigate)
        InfoButtonColumn(onNavigate)
    }
}

@Composable
fun MainMenuScreenLandscape(userName: String, onNavigate: (NavDirections) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        GreetingText(
            onClick = { onNavigate(actionMainMenuFragmentToUsernameDialog()) },
            userName = userName,
            modifier = Modifier.widthIn(0.dp, 320.dp)
        )
        Row {
            GameButtonColumn(onNavigate)
            InfoButtonColumn(onNavigate)
        }
    }
}

@Composable
fun GameButtonColumn(onNavigate: (NavDirections) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(actionMainMenuFragmentToMainActivity()) },
            iconId = R.drawable.ic_android_48dp,
            textId = R.string.menu_single_player,
        )
        MenuTextButton(
            onClick = { onNavigate(actionMainMenuFragmentToMultiplayerMenuFragment()) },
            iconId = R.drawable.ic_person_48dp,
            textId = R.string.menu_multi_player,
        )
    }
}

@Composable
fun InfoButtonColumn(onNavigate: (NavDirections) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(actionMainMenuFragmentToScoresActivity()) },
            iconId = R.drawable.ic_star_48dp,
            textId = R.string.scores_title,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(0.dp, 320.dp),
        ) {
            MenuIconButton(
                onClick = { onNavigate(actionMainMenuFragmentToSettingsActivity()) },
                iconId = R.drawable.ic_settings_48dp,
                textId = R.string.settings,
                modifier = Modifier.weight(1f),
            )
            MenuIconButton(
                onClick = { onNavigate(actionMainMenuFragmentToInfoActivity()) },
                iconId = R.drawable.ic_info_48dp,
                textId = R.string.application_info_label,
                modifier = Modifier.weight(1f),
            )
            MenuIconButton(
                onClick = { onNavigate(actionMainMenuFragmentToHowToActivity()) },
                iconId = R.drawable.ic_help_48dp,
                textId = R.string.help,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun GreetingText(
    onClick: () -> Unit,
    userName: String,
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier.padding(16.dp),
    ) {
        Surface {
            Text(
                text = stringResource(R.string.greetings, userName),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        TextButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.name)
            )
        }
    }
}

@Composable
fun MenuIconButton(
    onClick: () -> Unit,
    @DrawableRes iconId: Int,
    @StringRes textId: Int,
    modifier: Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier,
    ) {
        Icon(
            painter = painterResource(iconId),
            contentDescription = stringResource(textId),
            modifier = Modifier.size(24.dp),
        )
    }
}
