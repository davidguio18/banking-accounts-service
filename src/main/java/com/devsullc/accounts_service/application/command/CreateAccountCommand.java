package com.devsullc.accounts_service.application.command;

import com.devsullc.accounts_service.domain.model.AccountType;

import java.math.BigDecimal;

public record CreateAccountCommand(
        String number, AccountType type, Long customerId, BigDecimal openingBalance) {
}
