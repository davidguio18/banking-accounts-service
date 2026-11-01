package com.devsullc.accounts_service.application.command;

import com.devsullc.accounts_service.domain.model.MovementType;

import java.math.BigDecimal;

public record CreateMovementCommand(
        Long accountId, MovementType type, BigDecimal value, String description) {
}
