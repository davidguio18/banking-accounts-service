package com.devsullc.accounts_service.application.service;

import com.devsullc.accounts_service.application.command.CreateMovementCommand;
import com.devsullc.accounts_service.application.command.UpdateMovementCommand;
import com.devsullc.accounts_service.domain.exception.BusinessRuleException;
import com.devsullc.accounts_service.domain.exception.InsufficientBalanceException;
import com.devsullc.accounts_service.domain.exception.ResourceNotFoundException;
import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.model.Movement;
import com.devsullc.accounts_service.domain.model.MovementType;
import com.devsullc.accounts_service.domain.port.AccountRepositoryPort;
import com.devsullc.accounts_service.domain.port.CustomerStatusPort;
import com.devsullc.accounts_service.domain.port.MovementRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class MovementService {
    private final AccountRepositoryPort accounts;
    private final MovementRepositoryPort movements;
    private final CustomerStatusPort customers;

    public MovementService(
            AccountRepositoryPort accounts,
            MovementRepositoryPort movements,
            CustomerStatusPort customers) {
        this.accounts = accounts;
        this.movements = movements;
        this.customers = customers;
    }

    @Transactional
    public Movement create(CreateMovementCommand command) {
        Account account = lockOperationalAccount(command.accountId());
        validateValue(command.type(), command.value());
        BigDecimal newBalance = account.currentBalance().add(command.value());
        ensureAvailableBalance(newBalance);

        Movement movement = movements.save(new Movement(null, account.id(), OffsetDateTime.now(ZoneOffset.UTC),
                command.type(), command.value(), newBalance, command.description()));
        accounts.save(account.withBalance(newBalance));
        return movement;
    }

    @Transactional(readOnly = true)
    public List<Movement> findAll(Long accountId) {
        if (accountId == null) {
            return movements.findAll();
        }
        ensureAccountExists(accountId);
        return movements.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public Movement findById(Long id) {
        return movements.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movimiento", id));
    }

    @Transactional
    public Movement update(Long id, UpdateMovementCommand command) {
        Movement current = movements.lockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento", id));
        Map<Long, Account> lockedAccounts = lockAccounts(current.accountId(), command.accountId());
        Account oldAccount = lockedAccounts.get(current.accountId());
        Account newAccount = lockedAccounts.get(command.accountId());
        assertOperational(oldAccount);
        if (!newAccount.id().equals(oldAccount.id())) {
            assertOperational(newAccount);
        }
        validateValue(command.type(), command.value());

        Movement revised = new Movement(current.id(), newAccount.id(),
                command.date() == null ? current.date() : command.date(),
                command.type(), command.value(), current.balance(), command.description());
        movements.save(revised);
        recalculate(oldAccount);
        if (!newAccount.id().equals(oldAccount.id())) {
            recalculate(newAccount);
        }
        return movements.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movimiento", id));
    }

    private Account lockOperationalAccount(Long accountId) {
        Account account = accounts.lockById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", accountId));
        assertOperational(account);
        return account;
    }

    private void assertOperational(Account account) {
        if (!account.active()) {
            throw new BusinessRuleException("No se permiten operaciones sobre una cuenta inactiva");
        }
        customers.assertActive(account.customerId());
    }

    private Map<Long, Account> lockAccounts(Long firstId, Long secondId) {
        Map<Long, Account> locked = new TreeMap<>();
        List.of(firstId, secondId).stream().distinct().sorted().forEach(accountId ->
                locked.put(accountId, accounts.lockById(accountId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cuenta", accountId))));
        return locked;
    }

    private void recalculate(Account account) {
        BigDecimal balance = account.openingBalance();
        for (Movement movement : movements.findByAccountIdOrderByDateAndId(account.id())) {
            balance = balance.add(movement.value());
            ensureAvailableBalance(balance);
            movements.save(movement.withBalance(balance));
        }
        accounts.save(account.withBalance(balance));
    }

    private void validateValue(MovementType type, BigDecimal value) {
        if (value == null || value.signum() == 0) {
            throw new BusinessRuleException("El valor del movimiento no puede ser cero");
        }
        boolean validSign = type == MovementType.DEPOSITO ? value.signum() > 0 : value.signum() < 0;
        if (!validSign) {
            throw new BusinessRuleException("El signo del valor no corresponde al tipo de movimiento");
        }
    }

    private void ensureAvailableBalance(BigDecimal balance) {
        if (balance.signum() < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private void ensureAccountExists(Long accountId) {
        if (accounts.findById(accountId).isEmpty()) {
            throw new ResourceNotFoundException("Cuenta", accountId);
        }
    }
}
