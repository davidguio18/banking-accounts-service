package com.devsullc.accounts_service.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerMessagingConfiguration {
    @Bean
    TopicExchange customerEventsExchange(@Value("${messaging.customers.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    DirectExchange customerEventsDeadLetterExchange(@Value("${messaging.customers.exchange}") String exchange) {
        return new DirectExchange(exchange + ".dead", true, false);
    }

    @Bean
    Queue customerEventsQueue(
            @Value("${messaging.customers.queue}") String queue,
            @Value("${messaging.customers.exchange}") String exchange) {
        return QueueBuilder.durable(queue)
                .deadLetterExchange(exchange + ".dead")
                .deadLetterRoutingKey(queue + ".dead")
                .build();
    }

    @Bean
    Queue customerEventsDeadLetterQueue(@Value("${messaging.customers.queue}") String queue) {
        return QueueBuilder.durable(queue + ".dead").build();
    }

    @Bean
    Binding createdCustomerBinding(
            @Qualifier("customerEventsQueue") Queue customerEventsQueue,
            TopicExchange customerEventsExchange,
            @Value("${messaging.customers.created-routing-key}") String routingKey) {
        return BindingBuilder.bind(customerEventsQueue).to(customerEventsExchange).with(routingKey);
    }

    @Bean
    Binding customerStatusBinding(
            @Qualifier("customerEventsQueue") Queue customerEventsQueue,
            TopicExchange customerEventsExchange,
            @Value("${messaging.customers.status-routing-key}") String routingKey) {
        return BindingBuilder.bind(customerEventsQueue).to(customerEventsExchange).with(routingKey);
    }

    @Bean
    Binding customerDeadLetterBinding(
            @Qualifier("customerEventsDeadLetterQueue") Queue customerEventsDeadLetterQueue,
            DirectExchange customerEventsDeadLetterExchange,
            @Value("${messaging.customers.queue}") String queue) {
        return BindingBuilder.bind(customerEventsDeadLetterQueue)
                .to(customerEventsDeadLetterExchange).with(queue + ".dead");
    }

    @Bean
    MessageConverter customerMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter customerMessageConverter,
            @Value("${spring.rabbitmq.listener.simple.auto-startup:true}") boolean autoStartup) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(customerMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAutoStartup(autoStartup);
        return factory;
    }
}
