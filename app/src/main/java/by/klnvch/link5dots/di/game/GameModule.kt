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
package by.klnvch.link5dots.di.game

import by.klnvch.link5dots.di.game.bot.BotGameSubcomponent
import by.klnvch.link5dots.di.game.info.InfoGameSubcomponent
import by.klnvch.link5dots.di.game.nsd.NsdGameSubcomponent
import by.klnvch.link5dots.di.game.online.OnlineGameSubcomponent
import by.klnvch.link5dots.di.game.two.TwoPlayersGameSubcomponent
import by.klnvch.link5dots.ui.game.activities.BotGameActivity
import by.klnvch.link5dots.ui.game.activities.GameInfoActivity
import by.klnvch.link5dots.ui.game.activities.NsdGameActivity
import by.klnvch.link5dots.ui.game.activities.OnlineGameActivity
import by.klnvch.link5dots.ui.game.activities.TwoPlayersGameActivity
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module(
    subcomponents = [
        BotGameSubcomponent::class,
        TwoPlayersGameSubcomponent::class,
        InfoGameSubcomponent::class,
        OnlineGameSubcomponent::class,
        NsdGameSubcomponent::class,
    ]
)
internal abstract class GameModule {
    @Binds
    @IntoMap
    @ClassKey(BotGameActivity::class)
    abstract fun bindBotGameSubcomponentFactory(factory: BotGameSubcomponent.Factory): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(TwoPlayersGameActivity::class)
    abstract fun bindTwoPlayersGameSubcomponentFactory(factory: TwoPlayersGameSubcomponent.Factory): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(GameInfoActivity::class)
    abstract fun bindInfoGameSubcomponentFactory(factory: InfoGameSubcomponent.Factory): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(OnlineGameActivity::class)
    abstract fun bindOnlineGameSubcomponentFactory(factory: OnlineGameSubcomponent.Factory): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(NsdGameActivity::class)
    abstract fun bindNsdGameSubcomponentFactory(factory: NsdGameSubcomponent.Factory): AndroidInjector.Factory<*>
}
