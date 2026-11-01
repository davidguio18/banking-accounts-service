package com.devsullc.accounts_service.presentation.dto;

import com.devsullc.accounts_service.application.report.CustomerStatement;

import java.time.LocalDate;
import java.util.List;

public record StatementResponse(
        Long clienteId, LocalDate fechaInicio, LocalDate fechaFin, List<StatementAccountResponse> cuentas) {
    public static StatementResponse from(CustomerStatement statement) {
        return new StatementResponse(statement.customerId(), statement.startDate(), statement.endDate(),
                statement.accounts().stream().map(StatementAccountResponse::from).toList());
    }
}
