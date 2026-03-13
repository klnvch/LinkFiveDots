package by.klnvch.link5dots.di.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds

@Module
abstract class ViewModelFactoryModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Multibinds
    abstract fun viewModelMap(): Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>
}
