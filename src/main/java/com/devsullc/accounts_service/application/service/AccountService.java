package com.devsullc.accounts_service.application.service;

import com.devsullc.accounts_service.application.command.CreateAccountCommand;
import com.devsullc.accounts_service.application.command.UpdateAccountCommand;
import com.devsullc.accounts_service.domain.exception.BusinessRuleException;
import com.devsullc.accounts_service.domain.exception.ResourceNotFoundException;
import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.port.AccountRepositoryPort;
import com.devsullc.accounts_service.domain.port.CustomerStatusPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepositoryPort accounts;
    private final CustomerStatusPort customers;

    public AccountService(AccountRepositoryPort accounts, CustomerStatusPort customers) {
        this.accounts = accounts;
        this.customers = customers;
    }

    @Transactional
    public Account create(CreateAccountCommand command) {
        customers.assertActive(command.customerId());
        ensureUniqueNumber(command.number(), null);
        BigDecimal openingBalance = command.openingBalance();
        return accounts.save(new Account(null, command.number(), command.type(), command.customerId(),
                openingBalance, openingBalance, true));
    }

    @Transactional(readOnly = true)
    public List<Account> findAll(Long customerId) {
        return customerId == null ? accounts.findAll() : accounts.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accounts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cuenta", id));
    }

    @Transactional
    public Account update(Long id, UpdateAccountCommand command) {
        Account account = lockAccount(id);
        ensureUniqueNumber(command.number(), id);
        return accounts.save(account.withDetails(command.number(), command.type()));
    }

    @Transactional
    public Account changeStatus(Long id, boolean active) {
        Account account = lockAccount(id);
        if (active) {
            customers.assertActive(account.customerId());
        }
        return accounts.save(account.withActive(active));
    }

    @Transactional
    public void deactivateCustomerAccounts(Long customerId) {
        accounts.findByCustomerId(customerId).stream()
                .filter(account -> account.active())
                .map(account -> account.withActive(false))
                .forEach(accounts::save);
    }

    private Account lockAccount(Long id) {
        return accounts.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Cuenta", id));
    }

    private void ensureUniqueNumber(String number, Long accountId) {
        boolean alreadyUsed = accountId == null
                ? accounts.existsByNumber(number)
                : accounts.existsByNumberAndIdNot(number, accountId);
        if (alreadyUsed) {
            throw new BusinessRuleException("El número de cuenta ya existe");
        }
    }
}
