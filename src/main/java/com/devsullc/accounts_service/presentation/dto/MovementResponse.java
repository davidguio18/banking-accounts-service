package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.domain.model.Movement;
import com.devsullc.accounts_service.domain.model.MovementType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MovementResponse(
        Long movimientoId,
        Long cuentaId,
        OffsetDateTime fecha,
        MovementType tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo,
        String descripcion) {
    public static MovementResponse from(Movement movement) {
        return new MovementResponse(movement.id(), movement.accountId(), movement.date(), movement.type(),
                movement.value(), movement.balance(), movement.description());
    }
}
