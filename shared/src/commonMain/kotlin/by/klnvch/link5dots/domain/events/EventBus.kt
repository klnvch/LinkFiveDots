package by.klnvch.link5dots.domain.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
}

class DomainEventBus : DomainEventPublisher {
    private val _events = MutableSharedFlow<DomainEvent>(replay = 0)
    val events: SharedFlow<DomainEvent> get() = _events

    override suspend fun publish(event: DomainEvent) = _events.emit(event)
}
