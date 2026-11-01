package com.devsullc.accounts_service.infrastructure.persistence.entity;

import com.devsullc.accounts_service.domain.model.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "cuentas", schema = "accounts")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cuenta_id")
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 30)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private AccountType type;

    @Column(name = "cliente_id", nullable = false)
    private Long customerId;

    @Column(name = "saldo_inicial", nullable = false, precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "saldo_actual", nullable = false, precision = 18, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "estado", nullable = false)
    private boolean active;

    public AccountEntity() {
    }


}
