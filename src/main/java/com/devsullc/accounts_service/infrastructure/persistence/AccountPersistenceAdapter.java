package com.devsullc.accounts_service.infrastructure.persistence;

import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.port.AccountRepositoryPort;
import com.devsullc.accounts_service.infrastructure.persistence.entity.AccountEntity;
import com.devsullc.accounts_service.infrastructure.persistence.repository.SpringAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountPersistenceAdapter implements AccountRepositoryPort {
    private final SpringAccountRepository repository;

    public AccountPersistenceAdapter(SpringAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = account.id() == null
                ? new AccountEntity()
                : repository.findById(account.id()).orElseGet(AccountEntity::new);
        entity.setId(account.id());
        entity.setNumber(account.number());
        entity.setType(account.type());
        entity.setCustomerId(account.customerId());
        entity.setOpeningBalance(account.openingBalance());
        entity.setCurrentBalance(account.currentBalance());
        entity.setActive(account.active());
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Account> findById(Long id) {
        return repository.findById(id).map(AccountPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Account> lockById(Long id) {
        return repository.lockById(id).map(AccountPersistenceAdapter::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return repository.findAll().stream().map(AccountPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Account> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(AccountPersistenceAdapter::toDomain).toList();
    }

    @Override
    public boolean existsByNumber(String number) {
        return repository.existsByNumber(number);
    }

    @Override
    public boolean existsByNumberAndIdNot(String number, Long id) {
        return repository.existsByNumberAndIdNot(number, id);
    }

    private static Account toDomain(AccountEntity entity) {
        return new Account(entity.getId(), entity.getNumber(), entity.getType(), entity.getCustomerId(),
                entity.getOpeningBalance(), entity.getCurrentBalance(), entity.isActive());
    }
}
