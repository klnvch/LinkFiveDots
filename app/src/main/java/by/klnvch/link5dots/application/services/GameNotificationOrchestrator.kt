package by.klnvch.link5dots.application.services

import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.VibratorService
import by.klnvch.link5dots.domain.services.PlayerAttentionPolicy
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GameNotificationOrchestrator @Inject constructor(
    private val policy: PlayerAttentionPolicy,
    private val settings: Settings,
    private val vibratorService: VibratorService,
) {
    suspend fun notifyIfRequired(room: INetworkRoom) {
        if (!policy.isAttentionRequired(room)) return

        val isEnabled = settings.isVibrationEnabled.first()

        if (isEnabled) {
            vibratorService.vibrate()
        }
    }
}
