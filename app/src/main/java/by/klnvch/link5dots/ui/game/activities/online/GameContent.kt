/*
 * MIT License
 *
 * Copyright (c) 2025 klnvch
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

package by.klnvch.link5dots.ui.game.activities.online

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.klnvch.link5dots.R
import by.klnvch.link5dots.domain.models.NetworkGameAction
import by.klnvch.link5dots.domain.models.NightMode
import by.klnvch.link5dots.ui.common.TopBarTitle
import by.klnvch.link5dots.ui.common.tiledBackground
import by.klnvch.link5dots.ui.game.GameBottomAppBar
import by.klnvch.link5dots.ui.game.GameScreen
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.picker.PickerScreenError
import by.klnvch.link5dots.ui.game.picker.PickerScreenGame
import by.klnvch.link5dots.ui.game.picker.PickerScreenNone
import by.klnvch.link5dots.ui.game.topBar.TopBar
import by.klnvch.link5dots.ui.theme.AppTheme

private enum class MultiplayerRoute() { Picker, Game, Error }

@Composable
private fun GameTitle(action: NetworkGameAction, @StringRes defaultTitle: Int) {
    val id = when (action) {
        NetworkGameAction.PICKER_CREATING -> R.string.connecting
        NetworkGameAction.PICKER_DELETING -> R.string.connecting
        NetworkGameAction.PICKER_CREATED -> R.string.progress_text
        NetworkGameAction.PICKER_SCANNING -> R.string.searching
        NetworkGameAction.PICKER_CONNECTING -> R.string.connecting
        NetworkGameAction.GAME_OVER_WIN -> R.string.end_win
        NetworkGameAction.GAME_OVER_LOSE -> R.string.end_lose
        NetworkGameAction.GAME_DISCONNECTED -> R.string.disconnected
        NetworkGameAction.GAME_MOVE -> R.string.bt_message_your_turn
        NetworkGameAction.GAME_WAIT -> R.string.bt_message_opponents_turn
        NetworkGameAction.DEFAULT -> defaultTitle
    }
    TopBarTitle(id)
}

@Composable
fun GameContent(
    viewModel: OnlineGameViewModel,
    nightMode: NightMode,
    @StringRes defaultTitle: Int,
    pickerScreen: @Composable () -> Unit,
    errorScreen: @Composable (e: Throwable, onDone: (isSuccess: Boolean) -> Unit) -> Unit,
    onFinish: () -> Unit,
) {
    AppTheme(nightMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val action by viewModel.uiTitleState.collectAsState(NetworkGameAction.DEFAULT)
        val pickerUiState by viewModel.pickerUiState.collectAsState()
        val pickerScreen = pickerUiState.screen
        val disconnectDialog = remember { mutableStateOf(false) }

        val disconnectFinal: () -> Unit = {
            viewModel.exitGame()
            if (navBackStackEntry.isGameScreen()) navController.navigateUp() else onFinish()
        }

        val disconnectGuard: () -> Unit = {
            if (pickerScreen is PickerScreenGame) {
                disconnectDialog.value = true
            } else {
                disconnectFinal()
            }
        }

        LaunchedEffect(pickerScreen) {
            when (pickerScreen) {
                PickerScreenNone -> {}
                PickerScreenGame -> navController.navigate(MultiplayerRoute.Game.name)
                is PickerScreenError -> navController.navigate(MultiplayerRoute.Error.name)
            }
        }
        Scaffold(
            modifier = Modifier.tiledBackground(),
            containerColor = Color.Transparent,
            topBar = {
                if (navBackStackEntry.isGameScreen()) {
                    TopBar(
                        viewModel = viewModel,
                        title = { GameTitle(action, defaultTitle) },
                        navigateUp = disconnectGuard,
                    )
                } else {
                    PickerTopBar(
                        title = { GameTitle(action, defaultTitle) },
                        navigateUp = disconnectFinal,
                    )
                }
            },
            bottomBar = {
                if (navBackStackEntry.isGameScreen()) {
                    GameBottomAppBar(
                        viewModel = viewModel,
                        onNewGameNotImplemented = { disconnectGuard() },
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MultiplayerRoute.Picker.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = MultiplayerRoute.Picker.name) {
                    pickerScreen()
                }
                composable(route = MultiplayerRoute.Game.name) {
                    GameScreen(viewModel = viewModel)
                }
                composable(route = MultiplayerRoute.Error.name) {
                    if (pickerScreen is PickerScreenError)
                        errorScreen(pickerScreen.e) { isSuccess ->
                            when (isSuccess) {
                                true -> navController.navigateUp()
                                else -> onFinish()
                            }
                        }
                    else
                        navController.navigateUp()
                }
            }
            BackHandler(onBack = disconnectGuard)
        }

        if (disconnectDialog.value) {
            DisconnectDialog({
                disconnectDialog.value = false
                disconnectFinal()
            }, {
                disconnectDialog.value = false
            })
        }
    }
}

private fun NavBackStackEntry?.isGameScreen() =
    this?.destination?.route == MultiplayerRoute.Game.name
