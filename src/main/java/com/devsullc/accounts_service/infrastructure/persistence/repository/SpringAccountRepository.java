package com.devsullc.accounts_service.infrastructure.persistence.repository;

import com.devsullc.accounts_service.infrastructure.persistence.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringAccountRepository extends JpaRepository<AccountEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from AccountEntity account where account.id = :id")
    Optional<AccountEntity> lockById(@Param("id") Long id);

    List<AccountEntity> findByCustomerId(Long customerId);

    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);
}
