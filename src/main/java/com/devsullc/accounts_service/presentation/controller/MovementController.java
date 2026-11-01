package com.devsullc.accounts_service.presentation.controller;

import com.devsullc.accounts_service.application.command.CreateMovementCommand;
import com.devsullc.accounts_service.application.command.UpdateMovementCommand;
import com.devsullc.accounts_service.application.service.MovementService;
import com.devsullc.accounts_service.presentation.dto.CreateMovementRequest;
import com.devsullc.accounts_service.presentation.dto.MovementResponse;
import com.devsullc.accounts_service.presentation.dto.UpdateMovementRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/movimientos")
public class MovementController {
    private final MovementService service;

    public MovementController(MovementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse create(@Valid @RequestBody CreateMovementRequest request) {
        return MovementResponse.from(service.create(new CreateMovementCommand(request.cuentaId(),
                request.tipoMovimiento(), request.valor(), request.descripcion())));
    }

    @GetMapping
    public List<MovementResponse> findAll(@RequestParam(name = "cuentaId", required = false) Long cuentaId) {
        return service.findAll(cuentaId).stream().map(MovementResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MovementResponse findById(@PathVariable("id") Long id) {
        return MovementResponse.from(service.findById(id));
    }

    @PutMapping("/{id}")
    public MovementResponse update(
            @PathVariable("id") Long id, @Valid @RequestBody UpdateMovementRequest request) {
        return MovementResponse.from(service.update(id, new UpdateMovementCommand(request.cuentaId(),
                request.tipoMovimiento(), request.valor(), request.descripcion(), request.fecha())));
    }
}
