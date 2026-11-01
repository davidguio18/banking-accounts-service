package com.devsullc.accounts_service.infrastructure.messaging.consumer;

import com.devsullc.accounts_service.application.service.AccountService;
import com.devsullc.accounts_service.infrastructure.messaging.event.CustomerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CustomerEventListener.class);

    private final AccountService accounts;

    public CustomerEventListener(AccountService accounts) {
        this.accounts = accounts;
    }

    @RabbitListener(queues = "${messaging.customers.queue}")
    public void handleCustomerEvent(CustomerEvent event) {
        validate(event);
        if (isStatusChanged(event.eventType())) {
            if (!event.estado()) {
                accounts.deactivateCustomerAccounts(event.customerId());
            }
        } else if (!isCustomerCreated(event.eventType())) {
            throw new IllegalArgumentException("Tipo de evento de cliente no soportado: " + event.eventType());
        }
        logger.info("Evento de cliente procesado: {}", event.eventId());
    }

    private void validate(CustomerEvent event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()
                || event.customerId() == null || event.customerId() <= 0
                || event.nombre() == null || event.nombre().isBlank()
                || event.eventType() == null || event.eventType().isBlank()
                || event.estado() == null || event.occurredAt() == null) {
            throw new IllegalArgumentException("Evento de cliente inválido");
        }
    }

    private boolean isStatusChanged(String eventType) {
        return "CustomerStatusChangedEvent".equalsIgnoreCase(eventType)
                || "CUSTOMER_STATUS_CHANGED".equalsIgnoreCase(eventType)
                || "customer.status.changed".equalsIgnoreCase(eventType);
    }

    private boolean isCustomerCreated(String eventType) {
        return "CustomerCreatedEvent".equalsIgnoreCase(eventType)
                || "CUSTOMER_CREATED".equalsIgnoreCase(eventType)
                || "customer.created".equalsIgnoreCase(eventType);
    }
}
