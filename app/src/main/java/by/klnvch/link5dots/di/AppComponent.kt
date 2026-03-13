package by.klnvch.link5dots.di

import android.app.Application
import by.klnvch.link5dots.di.game.GameModule
import by.klnvch.link5dots.di.menu.MenuModule
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
        AppBindingModule2::class,
        NetworkModule::class,
        DatabaseModule::class,
        AndroidSupportInjectionModule::class,
        ViewModelFactoryModule::class,
        MenuModule::class,
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
