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

import androidx.annotation.StringRes
import by.klnvch.link5dots.R

enum class Route() {
    MainMenu, MultiplayerMenu, Scores, Info, Help, Settings
}

sealed class Screen() {
    open class ComposeScreen(route: Route, @StringRes val title: Int) : Screen() {
        val route = route.name
    }

    object MainMenu : ComposeScreen(Route.MainMenu, R.string.app_name)
    object MultiplayerMenu : ComposeScreen(Route.MultiplayerMenu, R.string.menu_multi_player)
    object Scores : ComposeScreen(Route.Scores, R.string.scores_title)
    object Info : ComposeScreen(Route.Info, R.string.application_info_label)
    object Help : ComposeScreen(Route.Help, R.string.help)
    object Settings : ComposeScreen(Route.Settings, R.string.settings)

    object BotGame : Screen()
    object MultiplayerTwo : Screen()
    object MultiplayerBluetooth : Screen()
    object MultiplayerNsd : Screen()
    object MultiplayerOnline : Screen()
    object SourceCode : Screen()
    object RateApp : Screen()
    object ShareApp : Screen()
    object Feedback : Screen()
    data class GameInfo(val key: String) : Screen()

    companion object {
        fun getComposeScreen(name: String?) = when (name) {
            Route.MainMenu.name -> MainMenu
            Route.MultiplayerMenu.name -> MultiplayerMenu
            Route.Scores.name -> Scores
            Route.Info.name -> Info
            Route.Help.name -> Help
            Route.Settings.name -> Settings
            else -> MainMenu
        }
    }
}
