package com.devsullc.accounts_service;

import com.devsullc.accounts_service.domain.port.CustomerStatusPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:accounts;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS accounts",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:integration-schema.sql",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=accounts",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "customers.service.base-url=http://localhost:9999/api"
})
@AutoConfigureMockMvc
@Sql(scripts = "/integration-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AccountsServiceApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerStatusPort customerStatus;

    @Test
    void createsMovementPersistsItAndUpdatesAccountBalance() throws Exception {
        doNothing().when(customerStatus).assertActive(2L);

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCuenta": "225487",
                                  "tipoCuenta": "CORRIENTE",
                                  "clienteId": 2,
                                  "saldoInicial": 100.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cuentaId").value(1));

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cuentaId": 1,
                                  "tipoMovimiento": "DEPOSITO",
                                  "valor": 600.00,
                                  "descripcion": "Depósito de prueba"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(700.0));

        mockMvc.perform(get("/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoActual").value(700.0));

        mockMvc.perform(get("/movimientos?cuentaId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].valor").value(600.0));

        mockMvc.perform(put("/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cuentaId": 1,
                                  "tipoMovimiento": "DEPOSITO",
                                  "valor": 500.00,
                                  "descripcion": "Movimiento corregido"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(600.0));

        mockMvc.perform(get("/cuentas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoActual").value(600.0));

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cuentaId": 1,
                                  "tipoMovimiento": "RETIRO",
                                  "valor": -1000.00,
                                  "descripcion": "Retiro rechazado"
                                }
                                """))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.error").value("SALDO_NO_DISPONIBLE"))
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }
}
