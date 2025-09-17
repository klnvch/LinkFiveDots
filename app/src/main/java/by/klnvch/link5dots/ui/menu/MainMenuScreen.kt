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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.MenuTextButton
import by.klnvch.link5dots.ui.common.TextNoSurface
import by.klnvch.link5dots.ui.common.UsernameDialog

@Composable
fun MainMenuScreen(
    getVMFactory: () -> ViewModelProvider.Factory,
    viewModel: MainMenuViewModel = viewModel(factory = getVMFactory()),
    onNavigate: (Screen) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val userName = uiState.userName
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> MainMenuScreenPortrait(userName, onNavigate) {
            viewModel.setUserName(it)
        }

        else -> MainMenuScreenLandscape(userName, onNavigate) {
            viewModel.setUserName(it)
        }
    }
}

@Composable
private fun MainMenuScreenPortrait(
    userName: String?,
    onNavigate: (Screen) -> Unit,
    onUserNameChanged: (userName: String?) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        GreetingText(
            userName = userName,
            onUserNameChanged = onUserNameChanged
        )
        GameButtonColumn(onNavigate)
        InfoButtonColumn(onNavigate)
    }
}

@Composable
private fun MainMenuScreenLandscape(
    userName: String?,
    onNavigate: (Screen) -> Unit,
    onUserNameChanged: (userName: String?) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        GreetingText(
            userName = userName,
            onUserNameChanged = onUserNameChanged,
        )
        Row {
            GameButtonColumn(onNavigate)
            InfoButtonColumn(onNavigate)
        }
    }
}

@Composable
private fun GameButtonColumn(onNavigate: (Screen) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(Screen.BotGame) },
            imageVector = Icons.Filled.Android,
            textId = R.string.menu_single_player,
        )
        MenuTextButton(
            onClick = { onNavigate(Screen.MultiplayerMenu) },
            imageVector = Icons.Filled.Person,
            textId = R.string.menu_multi_player,
        )
    }
}

@Composable
private fun InfoButtonColumn(onNavigate: (Screen) -> Unit) {
    Column {
        MenuTextButton(
            onClick = { onNavigate(Screen.Scores) },
            imageVector = Icons.Filled.Star,
            textId = R.string.scores_title,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(0.dp, 320.dp),
        ) {
            MenuIconButton(
                onClick = { onNavigate(Screen.Settings) },
                imageVector = Icons.Filled.Settings,
                textId = R.string.settings,
                modifier = Modifier.weight(1f),
            )
            MenuIconButton(
                onClick = { onNavigate(Screen.Info) },
                imageVector = Icons.Filled.Info,
                textId = R.string.application_info_label,
                modifier = Modifier.weight(1f),
            )
            MenuIconButton(
                onClick = { onNavigate(Screen.Help) },
                imageVector = Icons.AutoMirrored.Filled.Help,
                textId = R.string.help,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GreetingText(
    userName: String?,
    onUserNameChanged: (userName: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val openUserNameDialog = remember { mutableStateOf(false) }
    when {
        openUserNameDialog.value -> {
            UsernameDialog(userName, {
                openUserNameDialog.value = false
                onUserNameChanged(it)
            }, {
                openUserNameDialog.value = false
            })
        }
    }
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = modifier
            .widthIn(0.dp, 320.dp)
            .padding(16.dp),
    ) {
        val name = userName ?: stringResource(R.string.unknown)
        TextNoSurface(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.greetings, name),
            textAlign = TextAlign.Center,
        )
        TextButton(
            onClick = { openUserNameDialog.value = true }
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.name)
            )
        }
    }
}

@Composable
private fun MenuIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    @StringRes textId: Int,
    modifier: Modifier,
) {
    ElevatedButton(
        onClick = onClick,
        modifier,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(textId),
            modifier = Modifier.size(24.dp),
        )
    }
}
