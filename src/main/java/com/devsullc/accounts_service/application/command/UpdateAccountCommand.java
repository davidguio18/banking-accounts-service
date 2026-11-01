package com.devsullc.accounts_service.application.command;

import com.devsullc.accounts_service.domain.model.AccountType;

public record UpdateAccountCommand(String number, AccountType type) {
}
