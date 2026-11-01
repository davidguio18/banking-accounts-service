package com.devsullc.accounts_service.domain.exception;

public class CustomerServiceUnavailableException extends RuntimeException {
    public CustomerServiceUnavailableException() {
        super("No fue posible validar el estado del cliente");
    }
}
