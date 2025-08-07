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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import by.klnvch.link5dots.R
import by.klnvch.link5dots.ui.common.TopBarTitle
import by.klnvch.link5dots.ui.common.tiledBackground
import by.klnvch.link5dots.ui.game.GameScreen
import by.klnvch.link5dots.ui.game.OnlineGameViewModel
import by.klnvch.link5dots.ui.game.topBar.TopBar
import by.klnvch.link5dots.ui.theme.AppTheme

private enum class MultiplayerRoute() { Picker, Game, Error }

@Composable
private fun GameTitle(viewModel: OnlineGameViewModel, @StringRes defaultTitle: Int) {
    val titleId by viewModel.uiTitleState.collectAsState(defaultTitle)
    TopBarTitle(if (titleId == 0) defaultTitle else titleId)
}

@Composable
fun GameContent(
    viewModelFactory: ViewModelProvider.Factory,
    viewModel: OnlineGameViewModel,
    @StringRes defaultTitle: Int,
    pickerScreen: @Composable (getVMFactory: () -> ViewModelProvider.Factory) -> Unit,
    errorScreen: @Composable (e: Throwable, onDone: (isSuccess: Boolean) -> Unit) -> Unit,
    onFinish: () -> Unit,
) {
    AppTheme {
        val navController = rememberNavController()
        val event by viewModel.navigationEvent.collectAsState(PickerScreen)
        val disconnectDialog = remember { mutableStateOf(false) }

        val disconnectFinal: () -> Unit = {
            viewModel.cleanUp()
            if (navController.previousBackStackEntry != null) navController.navigateUp() else onFinish()
        }

        val disconnectGuard: () -> Unit = {
            if (navController.previousBackStackEntry != null && viewModel.isConnected()) {
                disconnectDialog.value = true
            } else {
                disconnectFinal()
            }
        }

        LaunchedEffect(event) {
            when (event) {
                PickerScreen -> navController.navigate(MultiplayerRoute.Picker.name)
                GameScreen -> navController.navigate(MultiplayerRoute.Game.name)
                is InitError -> navController.navigate(MultiplayerRoute.Error.name)
            }
        }
        Scaffold(
            modifier = Modifier.tiledBackground(ImageBitmap.imageResource(R.drawable.paper)),
            containerColor = Color.Transparent,
            topBar = {
                TopBar(
                    viewModel = viewModel,
                    title = { GameTitle(viewModel, defaultTitle) },
                    navigateUp = { disconnectGuard() }
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MultiplayerRoute.Picker.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = MultiplayerRoute.Picker.name) {
                    val getVMFactory: () -> ViewModelProvider.Factory =
                        remember { { viewModelFactory } }
                    pickerScreen(getVMFactory)
                }
                composable(route = MultiplayerRoute.Game.name) {
                    GameScreen(
                        viewModel = viewModel,
                    )
                }
                composable(route = MultiplayerRoute.Error.name) {
                    event.let {
                        if (it is InitError)
                            errorScreen(it.e) { isSuccess ->
                                when (isSuccess) {
                                    true -> navController.navigateUp()
                                    else -> onFinish()
                                }
                            }
                        else
                            navController.navigateUp()
                    }
                }
            }
        }

        if (disconnectDialog.value) {
            AlertDialog(
                text = {
                    Text(
                        text = stringResource(
                            R.string.is_disconnect_question,
                            viewModel.disconnectViewState.name
                        )
                    )
                },
                onDismissRequest = { disconnectDialog.value = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            disconnectDialog.value = false
                            disconnectFinal()
                        }
                    ) {
                        Text(text = stringResource(R.string.okay))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { disconnectDialog.value = false }
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
