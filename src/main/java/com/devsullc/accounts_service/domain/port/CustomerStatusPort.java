package com.devsullc.accounts_service.domain.port;

public interface CustomerStatusPort {
    void assertActive(Long customerId);
}
