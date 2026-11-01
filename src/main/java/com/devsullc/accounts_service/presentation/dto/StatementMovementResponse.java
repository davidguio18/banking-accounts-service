package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.domain.model.Movement;
import com.devsullc.accounts_service.domain.model.MovementType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record StatementMovementResponse(
        OffsetDateTime fecha, MovementType tipoMovimiento, BigDecimal valor, BigDecimal saldo) {
    public static StatementMovementResponse from(Movement movement) {
        return new StatementMovementResponse(movement.date(), movement.type(), movement.value(), movement.balance());
    }
}
