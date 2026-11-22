package com.example.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.queue.notification}")
    private String notificationQueue;

    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;

    @Value("${rabbitmq.routing.updated:task.updated}")
    private String updatedRoutingKey;

    @Value("${rabbitmq.dlx.name:task.dlx}")
    private String dlxName;

    @Value("${rabbitmq.queue.notification-dlq:notification.dlq}")
    private String dlqQueueName;

    @Value("${rabbitmq.routing.dlq:task.dlq.routing}")
    private String dlqRoutingKey;

    @Bean
    public TopicExchange taskExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxName);
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(dlqQueueName).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(notificationDlq())
                .to(dlxExchange())
                .with(dlqRoutingKey);
    }

    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxName);
        args.put("x-dead-letter-routing-key", dlqRoutingKey);
        return QueueBuilder.durable(notificationQueue).withArguments(args).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(taskExchange())
                .with(createdRoutingKey);
    }

    @Bean
    public Binding notificationUpdatedBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(taskExchange())
                .with(updatedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
