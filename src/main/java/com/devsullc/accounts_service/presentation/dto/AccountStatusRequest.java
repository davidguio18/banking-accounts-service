package com.devsullc.accounts_service.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record AccountStatusRequest(@NotNull Boolean estado) {
}
