package by.klnvch.link5dots.di.game.info

import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.di.viewmodels.ViewModelKey
import by.klnvch.link5dots.ui.game.viewmodels.InfoGameViewModel
import by.klnvch.link5dots.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class InfoGameViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(InfoGameViewModel::class)
    abstract fun bindInfoGameViewModel(viewModel: InfoGameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
}
