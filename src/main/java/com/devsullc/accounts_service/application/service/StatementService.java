package com.devsullc.accounts_service.application.service;

import com.devsullc.accounts_service.application.report.AccountStatement;
import com.devsullc.accounts_service.application.report.CustomerStatement;
import com.devsullc.accounts_service.domain.exception.BusinessRuleException;
import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.model.Movement;
import com.devsullc.accounts_service.domain.port.AccountRepositoryPort;
import com.devsullc.accounts_service.domain.port.MovementRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatementService {
    private final AccountRepositoryPort accounts;
    private final MovementRepositoryPort movements;

    public StatementService(AccountRepositoryPort accounts, MovementRepositoryPort movements) {
        this.accounts = accounts;
        this.movements = movements;
    }

    @Transactional(readOnly = true)
    public CustomerStatement getStatement(LocalDate startDate, LocalDate endDate, Long customerId) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessRuleException("fechaInicio no puede ser posterior a fechaFin");
        }
        List<Account> customerAccounts = accounts.findByCustomerId(customerId);
        List<Long> accountIds = customerAccounts.stream().map(account -> account.id()).toList();
        List<Movement> rangeMovements = accountIds.isEmpty()
                ? List.of()
                : movements.findByAccountIdsAndDateRange(accountIds, startDate.atStartOfDay().atOffset(ZoneOffset.UTC),
                        endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC));
        Map<Long, List<Movement>> movementsByAccount = rangeMovements.stream()
                .collect(Collectors.groupingBy(movement -> movement.accountId()));

        List<AccountStatement> statements = new ArrayList<>();
        for (Account account : customerAccounts) {
            statements.add(new AccountStatement(account,
                    movementsByAccount.getOrDefault(account.id(), List.of())));
        }
        return new CustomerStatement(customerId, startDate, endDate, List.copyOf(statements));
    }
}
