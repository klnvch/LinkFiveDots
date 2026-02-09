package by.klnvch.link5dots.application.handlers

import by.klnvch.link5dots.domain.events.DomainEventBus
import by.klnvch.link5dots.domain.events.DomainHandler
import by.klnvch.link5dots.domain.events.NetworkRoomUpdatedEvent
import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.repositories.Settings
import by.klnvch.link5dots.domain.repositories.VibratorService
import by.klnvch.link5dots.domain.services.PlayerAttentionPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameNotificationHandler @Inject constructor(
    private val eventBus: DomainEventBus,
    private val policy: PlayerAttentionPolicy,
    private val settings: Settings,
    private val vibratorService: VibratorService,
) : DomainHandler {
    init {
        CoroutineScope(Dispatchers.Default).launch {
            eventBus.events.filterIsInstance<NetworkRoomUpdatedEvent>().collect { event ->
                notifyIfRequired(event.room)
            }
        }
    }

    private suspend fun notifyIfRequired(room: INetworkRoom) {
        if (!policy.isAttentionRequired(room)) return

        val isEnabled = settings.isVibrationEnabled.first()

        if (isEnabled) {
            vibratorService.vibrate()
        }
    }
}
