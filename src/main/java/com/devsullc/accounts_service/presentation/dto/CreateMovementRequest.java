package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.domain.model.MovementType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateMovementRequest(
        @NotNull @Positive Long cuentaId,
        @NotNull MovementType tipoMovimiento,
        @NotNull @Digits(integer = 16, fraction = 2) BigDecimal valor,
        @Size(max = 250) String descripcion) {
}
