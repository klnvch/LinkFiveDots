package by.klnvch.link5dots.di.menu

import androidx.lifecycle.ViewModel
import by.klnvch.link5dots.di.viewmodels.ViewModelKey
import by.klnvch.link5dots.ui.menu.MainMenuViewModel
import by.klnvch.link5dots.ui.scores.ScoresViewModel
import by.klnvch.link5dots.ui.scores.history.HistoryViewModel
import by.klnvch.link5dots.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MenuViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainMenuViewModel::class)
    abstract fun bindMainMenuViewModel(viewModel: MainMenuViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistoryViewModel::class)
    abstract fun bindHistoryViewModel(viewModel: HistoryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScoresViewModel::class)
    abstract fun bindScoresViewModel(viewModel: ScoresViewModel): ViewModel
}
