package by.klnvch.link5dots.domain.usecases

import by.klnvch.link5dots.domain.repositories.Settings
import javax.inject.Inject

class CheckTheFirstRunUseCase @Inject constructor(val settings: Settings) {
    suspend fun check() {
        settings.isFirstRun().collect { settings.setFirstRun() }
    }
}
