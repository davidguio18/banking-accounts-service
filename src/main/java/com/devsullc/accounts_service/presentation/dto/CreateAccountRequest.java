package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.domain.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 30) String numeroCuenta,
        @NotNull AccountType tipoCuenta,
        @NotNull @Positive Long clienteId,
        @NotNull @DecimalMin("0.00") @Digits(integer = 16, fraction = 2) BigDecimal saldoInicial) {
}
