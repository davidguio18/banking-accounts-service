package com.devsullc.accounts_service.infrastructure.persistence;

import com.devsullc.accounts_service.domain.model.Movement;
import com.devsullc.accounts_service.domain.port.MovementRepositoryPort;
import com.devsullc.accounts_service.infrastructure.persistence.entity.MovementEntity;
import com.devsullc.accounts_service.infrastructure.persistence.repository.SpringAccountRepository;
import com.devsullc.accounts_service.infrastructure.persistence.repository.SpringMovementRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MovementPersistenceAdapter implements MovementRepositoryPort {
    private final SpringMovementRepository repository;
    private final SpringAccountRepository accountRepository;

    public MovementPersistenceAdapter(
            SpringMovementRepository repository, SpringAccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Movement save(Movement movement) {
        MovementEntity entity = movement.id() == null
                ? new MovementEntity()
                : repository.findById(movement.id()).orElseGet(MovementEntity::new);
        entity.setId(movement.id());
        entity.setAccount(accountRepository.getReferenceById(movement.accountId()));
        entity.setDate(movement.date());
        entity.setType(movement.type());
        entity.setValue(movement.value());
        entity.setBalance(movement.balance());
        entity.setDescription(movement.description());
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Movement> findById(Long id) {
        return repository.findById(id).map(MovementPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Movement> lockById(Long id) {
        return repository.lockById(id).map(MovementPersistenceAdapter::toDomain);
    }

    @Override
    public List<Movement> findByAccountIdOrderByDateAndId(Long accountId) {
        return repository.findByAccountIdOrderByDateAscIdAsc(accountId).stream()
                .map(MovementPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Movement> findByAccountIdsAndDateRange(
            List<Long> accountIds, OffsetDateTime startInclusive, OffsetDateTime endExclusive) {
        return repository.findByAccountIdInAndDateGreaterThanEqualAndDateLessThanOrderByAccountIdAscDateAscIdAsc(
                        accountIds, startInclusive, endExclusive).stream()
                .map(MovementPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Movement> findAll() {
        return repository.findAll().stream().map(MovementPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Movement> findByAccountId(Long accountId) {
        return repository.findByAccountId(accountId).stream().map(MovementPersistenceAdapter::toDomain).toList();
    }

    private static Movement toDomain(MovementEntity entity) {
        return new Movement(entity.getId(), entity.getAccount().getId(), entity.getDate(), entity.getType(),
                entity.getValue(), entity.getBalance(), entity.getDescription());
    }
}
