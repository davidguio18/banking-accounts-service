package com.devsullc.accounts_service.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Movement(
        Long id,
        Long accountId,
        OffsetDateTime date,
        MovementType type,
        BigDecimal value,
        BigDecimal balance,
        String description) {

    public Movement withId(Long newId) {
        return new Movement(newId, accountId, date, type, value, balance, description);
    }

    public Movement withBalance(BigDecimal newBalance) {
        return new Movement(id, accountId, date, type, value, newBalance, description);
    }
}
