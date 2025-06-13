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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.klnvch.link5dots.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    currentScreen: Screen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                stringResource(currentScreen.title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(33, 33, 33),
            titleContentColor = Color(250, 250, 250),
            navigationIconContentColor = Color(250, 250, 250),
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            }
        }
    )
}

@Composable
fun App(
    viewModel: MainMenuViewModel,
    navController: NavHostController = rememberNavController(),
    onNavigate: (Screen) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.valueOf(
        backStackEntry?.destination?.route ?: Screen.MainMenu.name
    )

    Scaffold(
        modifier = Modifier.background(
            ShaderBrush(
                ImageShader(
                    ImageBitmap.imageResource(R.drawable.paper),
                    TileMode.Repeated,
                    TileMode.Repeated
                )
            )
        ),
        containerColor = Color.Transparent,
        topBar = {
            AppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MainMenu.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = Screen.MainMenu.name) {
                MainMenuScreen(viewModel) {
                    when (it) {
                        Screen.MultiplayerMenu -> navController.navigate(Screen.MultiplayerMenu.name)
                        Screen.Info -> navController.navigate(Screen.Info.name)
                        Screen.Help -> navController.navigate(Screen.Help.name)
                        else -> onNavigate(it)
                    }
                }
            }
            composable(route = Screen.MultiplayerMenu.name) {
                MultiplayerMenuScreen(onNavigate)
            }
            composable(route = Screen.Info.name) {
                InfoScreen { onNavigate(it) }
            }
            composable(route = Screen.Help.name) {
                HelpScreen()
            }
        }
    }
}
