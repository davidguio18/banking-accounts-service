package com.devsullc.accounts_service.infrastructure.persistence.repository;

import com.devsullc.accounts_service.infrastructure.persistence.entity.MovementEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringMovementRepository extends JpaRepository<MovementEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select movement from MovementEntity movement where movement.id = :id")
    Optional<MovementEntity> lockById(@Param("id") Long id);

    List<MovementEntity> findByAccountIdOrderByDateAscIdAsc(Long accountId);

    List<MovementEntity> findByAccountIdInAndDateGreaterThanEqualAndDateLessThanOrderByAccountIdAscDateAscIdAsc(
            List<Long> accountIds, OffsetDateTime startInclusive, OffsetDateTime endExclusive);

    List<MovementEntity> findByAccountId(Long accountId);
}
