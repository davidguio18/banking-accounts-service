package com.devsullc.accounts_service.infrastructure.customer;

import com.devsullc.accounts_service.domain.exception.BusinessRuleException;
import com.devsullc.accounts_service.domain.exception.CustomerServiceUnavailableException;
import com.devsullc.accounts_service.domain.exception.ResourceNotFoundException;
import com.devsullc.accounts_service.domain.port.CustomerStatusPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CustomerStatusAdapter implements CustomerStatusPort {
    private final RestClient restClient;

    public CustomerStatusAdapter(
            RestClient.Builder builder,
            @Value("${customers.service.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void assertActive(Long customerId) {
        CustomerStatusResponse customer;
        try {
            customer = restClient.get()
                    .uri("/clientes/{id}", customerId)
                    .retrieve()
                    .body(CustomerStatusResponse.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Cliente", customerId);
        } catch (RestClientException exception) {
            throw new CustomerServiceUnavailableException();
        }
        if (customer == null || customer.estado() == null) {
            throw new CustomerServiceUnavailableException();
        }
        if (!customer.estado()) {
            throw new BusinessRuleException("No se permiten operaciones para un cliente inactivo");
        }
    }
}
