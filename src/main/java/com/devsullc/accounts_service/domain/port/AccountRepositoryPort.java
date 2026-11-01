package com.devsullc.accounts_service.domain.port;

import com.devsullc.accounts_service.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryPort {
    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> lockById(Long id);

    List<Account> findAll();

    List<Account> findByCustomerId(Long customerId);

    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);
}
