package com.devsullc.accounts_service.domain.port;

import com.devsullc.accounts_service.domain.model.Movement;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MovementRepositoryPort {
    Movement save(Movement movement);

    Optional<Movement> findById(Long id);

    Optional<Movement> lockById(Long id);

    List<Movement> findByAccountIdOrderByDateAndId(Long accountId);

    List<Movement> findByAccountIdsAndDateRange(
            List<Long> accountIds, OffsetDateTime startInclusive, OffsetDateTime endExclusive);

    List<Movement> findAll();

    List<Movement> findByAccountId(Long accountId);
}
