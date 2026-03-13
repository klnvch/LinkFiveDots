package by.klnvch.link5dots.di.game

import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.di.viewmodels.ViewModelKey
import by.klnvch.link5dots.ui.game.viewmodels.OfflineGameViewModel
import by.klnvch.link5dots.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OfflineGameViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(OfflineGameViewModel::class)
    abstract fun bindMainMenuViewModel(viewModel: OfflineGameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
}
