package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.model.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long cuentaId,
        String numeroCuenta,
        AccountType tipoCuenta,
        Long clienteId,
        BigDecimal saldoInicial,
        BigDecimal saldoActual,
        boolean estado) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.id(), account.number(), account.type(), account.customerId(),
                account.openingBalance(), account.currentBalance(), account.active());
    }
}
