package com.devsullc.accounts_service.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " no encontrado: " + id);
    }
}
