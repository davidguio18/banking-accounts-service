package com.devsullc.accounts_service.infrastructure.persistence.entity;

import com.devsullc.accounts_service.domain.model.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "movimientos", schema = "accounts")
public class MovementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movimiento_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private AccountEntity account;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private MovementType type;

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    private BigDecimal value;

    @Column(name = "saldo", nullable = false, precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "descripcion", length = 250)
    private String description;

    public MovementEntity() {
    }

}
