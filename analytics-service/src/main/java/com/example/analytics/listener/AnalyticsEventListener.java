package com.example.analytics.listener;

import com.example.analytics.service.AnalyticsService;
import com.example.task.event.TaskCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);

    private final AnalyticsService analyticsService;

    public AnalyticsEventListener(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.analytics}")
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.info("Analytics Service recebeu TASK_CREATED: ID {}", event.getTaskId());
        analyticsService.recordTaskCreated(event.getEventId(), event.getTaskId(), event.getPriority());
    }
}
