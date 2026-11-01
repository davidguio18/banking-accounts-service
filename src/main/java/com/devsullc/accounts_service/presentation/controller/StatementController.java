package com.devsullc.accounts_service.presentation.controller;

import com.devsullc.accounts_service.application.service.StatementService;
import com.devsullc.accounts_service.presentation.dto.StatementResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
public class StatementController {
    private final StatementService service;

    public StatementController(StatementService service) {
        this.service = service;
    }

    @GetMapping
    public StatementResponse getStatement(
            @RequestParam(name = "fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "clienteId") Long clienteId) {
        return StatementResponse.from(service.getStatement(fechaInicio, fechaFin, clienteId));
    }
}
