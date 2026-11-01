package com.devsullc.accounts_service.application.service;

import com.devsullc.accounts_service.application.command.CreateMovementCommand;
import com.devsullc.accounts_service.domain.exception.BusinessRuleException;
import com.devsullc.accounts_service.domain.exception.InsufficientBalanceException;
import com.devsullc.accounts_service.domain.model.Account;
import com.devsullc.accounts_service.domain.model.AccountType;
import com.devsullc.accounts_service.domain.model.Movement;
import com.devsullc.accounts_service.domain.model.MovementType;
import com.devsullc.accounts_service.domain.port.AccountRepositoryPort;
import com.devsullc.accounts_service.domain.port.CustomerStatusPort;
import com.devsullc.accounts_service.domain.port.MovementRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementServiceTest {
    @Mock
    private AccountRepositoryPort accounts;
    @Mock
    private MovementRepositoryPort movements;
    @Mock
    private CustomerStatusPort customers;

    private MovementService service;

    @BeforeEach
    void setUp() {
        service = new MovementService(accounts, movements, customers);
        lenient().when(accounts.lockById(12L)).thenReturn(Optional.of(account(true)));
    }

    private void stubSuccessfulPersistence() {
        when(accounts.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movements.save(any(Movement.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void depositPersistsMovementAndUpdatesBalance() {
        stubSuccessfulPersistence();
        Movement movement = service.create(new CreateMovementCommand(
                12L, MovementType.DEPOSITO, new BigDecimal("60.00"), "Abono"));

        assertEquals(new BigDecimal("160.00"), movement.balance());
        verify(accounts).save(any(Account.class));
        verify(movements).save(any(Movement.class));
    }

    @Test
    void withdrawalPersistsNegativeValueAndUpdatesBalance() {
        stubSuccessfulPersistence();
        Movement movement = service.create(new CreateMovementCommand(
                12L, MovementType.RETIRO, new BigDecimal("-40.00"), "Retiro"));

        assertEquals(new BigDecimal("-40.00"), movement.value());
        assertEquals(new BigDecimal("60.00"), movement.balance());
    }

    @Test
    void rejectsWithdrawalThatExceedsAvailableBalance() {
        assertThrows(InsufficientBalanceException.class, () -> service.create(new CreateMovementCommand(
                12L, MovementType.RETIRO, new BigDecimal("-101.00"), "Retiro")));

        verify(movements, never()).save(any(Movement.class));
        verify(accounts, never()).save(any(Account.class));
    }

    @Test
    void rejectsOperationOnInactiveAccount() {
        when(accounts.lockById(12L)).thenReturn(Optional.of(account(false)));

        assertThrows(BusinessRuleException.class, () -> service.create(new CreateMovementCommand(
                12L, MovementType.DEPOSITO, new BigDecimal("1.00"), null)));
        verify(movements, never()).save(any(Movement.class));
    }

    @Test
    void rejectsOperationWhenCustomerIsInactive() {
        org.mockito.Mockito.doThrow(new BusinessRuleException("Cliente inactivo"))
                .when(customers).assertActive(7L);

        assertThrows(BusinessRuleException.class, () -> service.create(new CreateMovementCommand(
                12L, MovementType.DEPOSITO, new BigDecimal("1.00"), null)));
        verify(movements, never()).save(any(Movement.class));
    }

    private Account account(boolean active) {
        return new Account(12L, "ACC-12", AccountType.AHORROS, 7L,
                new BigDecimal("100.00"), new BigDecimal("100.00"), active);
    }
}
