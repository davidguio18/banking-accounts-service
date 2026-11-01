package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.application.report.AccountStatement;
import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.model.AccountType;

import java.math.BigDecimal;
import java.util.List;

public record StatementAccountResponse(
        String numeroCuenta,
        AccountType tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoActual,
        boolean estado,
        List<StatementMovementResponse> movimientos) {
    public static StatementAccountResponse from(AccountStatement statement) {
        Account account = statement.account();
        return new StatementAccountResponse(account.number(), account.type(), account.openingBalance(),
                account.currentBalance(), account.active(),
                statement.movements().stream().map(StatementMovementResponse::from).toList());
    }
}
