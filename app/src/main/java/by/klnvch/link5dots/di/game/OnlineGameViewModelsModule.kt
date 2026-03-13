package by.klnvch.link5dots.di.game

import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.di.viewmodels.ViewModelKey
import by.klnvch.link5dots.ui.game.picker.VisibilityViewModel
import by.klnvch.link5dots.ui.game.viewmodels.OnlineGameViewModel
import by.klnvch.link5dots.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OnlineGameViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(OnlineGameViewModel::class)
    abstract fun bindOnlineGameViewModel(viewModel: OnlineGameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VisibilityViewModel::class)
    abstract fun bindVisibilityViewModel(viewModel: VisibilityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
}
