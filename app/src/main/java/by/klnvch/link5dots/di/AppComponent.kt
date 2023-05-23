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
package by.klnvch.link5dots.di

import android.app.Application
import by.klnvch.link5dots.di.game.GameModule
import by.klnvch.link5dots.di.menu.MenuModule
import by.klnvch.link5dots.di.scores.ScoresModule
import by.klnvch.link5dots.di.settings.SettingsModule
import by.klnvch.link5dots.di.viewmodels.ViewModelFactoryModule
import by.klnvch.link5dots.di.workers.WorkerSubcomponent
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppBindingModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        DataStoreModule::class,
        ServiceBindingModule::class,
        ActivityBindingModule::class,
        AndroidSupportInjectionModule::class,
        ViewModelFactoryModule::class,
        SettingsModule::class,
        MenuModule::class,
        ScoresModule::class,
        GameModule::class,
    ]
)
interface AppComponent : AndroidInjector<MyApp> {

    fun workerSubcomponentBuilder(): WorkerSubcomponent.Builder

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
}
