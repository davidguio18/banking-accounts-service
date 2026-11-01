package com.devsullc.accounts_service.infrastructure.customer;

import com.fasterxml.jackson.annotation.JsonAlias;

public record CustomerStatusResponse(@JsonAlias("status") Boolean estado) {
}
