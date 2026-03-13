package by.klnvch.link5dots.di.menu

import by.klnvch.link5dots.di.ActivityScope
import by.klnvch.link5dots.ui.menu.MenuActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@ActivityScope
@Subcomponent(
    modules = [MenuViewModelsModule::class]
)
interface MenuSubcomponent : AndroidInjector<MenuActivity> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<MenuActivity>
}
