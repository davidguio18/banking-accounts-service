package com.devsullc.accounts_service.application.report;

import java.time.LocalDate;
import java.util.List;

public record CustomerStatement(
        Long customerId, LocalDate startDate, LocalDate endDate, List<AccountStatement> accounts) {
}
