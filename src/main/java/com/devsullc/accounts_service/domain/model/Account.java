package com.devsullc.accounts_service.domain.model;

import java.math.BigDecimal;

public record Account(
        Long id,
        String number,
        AccountType type,
        Long customerId,
        BigDecimal openingBalance,
        BigDecimal currentBalance,
        boolean active) {

    public Account withId(Long newId) {
        return new Account(newId, number, type, customerId, openingBalance, currentBalance, active);
    }

    public Account withDetails(String newNumber, AccountType newType) {
        return new Account(id, newNumber, newType, customerId, openingBalance, currentBalance, active);
    }

    public Account withBalance(BigDecimal newBalance) {
        return new Account(id, number, type, customerId, openingBalance, newBalance, active);
    }

    public Account withActive(boolean newActive) {
        return new Account(id, number, type, customerId, openingBalance, currentBalance, newActive);
    }
}
