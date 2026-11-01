package com.devsullc.accounts_service.presentation.dto;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        OffsetDateTime timestamp, int status, String error, String message, String path) {
}
