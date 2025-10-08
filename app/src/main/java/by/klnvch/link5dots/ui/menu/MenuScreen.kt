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

package by.klnvch.link5dots.ui.menu

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.klnvch.link5dots.di.viewmodels.SavedStateViewModelFactory
import by.klnvch.link5dots.ui.common.NavigationIcon
import by.klnvch.link5dots.ui.common.TopBarTitle
import by.klnvch.link5dots.ui.common.tiledBackground
import by.klnvch.link5dots.ui.common.topAppBarColors
import by.klnvch.link5dots.ui.scores.ScoresScreen
import by.klnvch.link5dots.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: Screen.ComposeScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        title = { TopBarTitle(currentScreen.title) },
        colors = topAppBarColors(),
        modifier = modifier,
        navigationIcon = { if (canNavigateBack) NavigationIcon(onClick = navigateUp) }
    )
}

@Composable
fun App(
    getVMFactory: () -> ViewModelProvider.Factory,
    getSSVMFactory: () -> SavedStateViewModelFactory,
    navController: NavHostController = rememberNavController(),
    onNavigate: (Screen) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.getComposeScreen(backStackEntry?.destination?.route)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.tiledBackground(),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MainMenu.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Route.MainMenu.name) {
                MainMenuScreen(getVMFactory) {
                    if (it is Screen.ComposeScreen) {
                        navController.navigate(it.route)
                    } else {
                        onNavigate(it)
                    }
                }
            }
            composable(route = Route.MultiplayerMenu.name) {
                MultiplayerMenuScreen(onNavigate)
            }
            composable(route = Route.Scores.name) {
                ScoresScreen(
                    getSSVMFactory,
                    onNavigate = { onNavigate(it) },
                    onSnackbarMessage = { message, actionLabel, action ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = actionLabel,
                                duration = SnackbarDuration.Long
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> action()
                                SnackbarResult.Dismissed -> {}
                            }
                        }
                    }
                )
            }
            composable(route = Route.Info.name) {
                InfoScreen { onNavigate(it) }
            }
            composable(route = Route.Help.name) {
                HelpScreen()
            }
            composable(route = Route.Settings.name) {
                SettingsScreen(getVMFactory)
            }
        }
    }
}
