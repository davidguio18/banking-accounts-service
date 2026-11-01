package com.devsullc.accounts_service.infrastructure.messaging.event;

import java.time.OffsetDateTime;

public record CustomerEvent(
        String eventId,
        Long customerId,
        String nombre,
        Boolean estado,
        String eventType,
        OffsetDateTime occurredAt) {
}
