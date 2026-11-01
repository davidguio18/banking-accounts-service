package com.devsullc.accounts_service.application.report;

import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.model.Movement;

import java.util.List;

public record AccountStatement(Account account, List<Movement> movements) {
}
