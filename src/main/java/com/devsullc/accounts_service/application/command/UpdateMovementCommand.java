package com.devsullc.accounts_service.application.command;

import com.devsullc.accounts_service.domain.model.MovementType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdateMovementCommand(
        Long accountId,
        MovementType type,
        BigDecimal value,
        String description,
        OffsetDateTime date) {
}
