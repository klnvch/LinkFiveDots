package by.klnvch.link5dots.domain.events

import by.klnvch.link5dots.domain.models.INetworkRoom
import by.klnvch.link5dots.domain.models.online.OnlineRoomLive

interface DomainEvent

data class OnlineRoomUpdatedEvent(
    val previous: OnlineRoomLive?,
    val current: OnlineRoomLive,
) : DomainEvent

data class NetworkRoomUpdatedEvent(val room: INetworkRoom) : DomainEvent
