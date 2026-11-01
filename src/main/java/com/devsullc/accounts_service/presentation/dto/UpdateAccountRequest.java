package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.domain.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank @Size(max = 30) String numeroCuenta,
        @NotNull AccountType tipoCuenta) {
}
