package com.devsullc.accounts_service.presentation.controller;

import com.devsullc.accounts_service.application.command.CreateAccountCommand;
import com.devsullc.accounts_service.application.command.UpdateAccountCommand;
import com.devsullc.accounts_service.application.service.AccountService;
import com.devsullc.accounts_service.presentation.dto.AccountResponse;
import com.devsullc.accounts_service.presentation.dto.AccountStatusRequest;
import com.devsullc.accounts_service.presentation.dto.CreateAccountRequest;
import com.devsullc.accounts_service.presentation.dto.UpdateAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(service.create(new CreateAccountCommand(request.numeroCuenta(),
                request.tipoCuenta(), request.clienteId(), request.saldoInicial())));
    }

    @GetMapping
    public List<AccountResponse> findAll(@RequestParam(name = "clienteId", required = false) Long clienteId) {
        return service.findAll(clienteId).stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AccountResponse findById(@PathVariable("id") Long id) {
        return AccountResponse.from(service.findById(id));
    }

    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable("id") Long id, @Valid @RequestBody UpdateAccountRequest request) {
        return AccountResponse.from(service.update(id,
                new UpdateAccountCommand(request.numeroCuenta(), request.tipoCuenta())));
    }

    @PatchMapping("/{id}/estado")
    public AccountResponse changeStatus(
            @PathVariable("id") Long id, @Valid @RequestBody AccountStatusRequest request) {
        return AccountResponse.from(service.changeStatus(id, request.estado()));
    }
}
