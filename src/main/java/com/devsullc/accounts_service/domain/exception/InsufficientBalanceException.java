package com.devsullc.accounts_service.domain.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Saldo no disponible");
    }
}
