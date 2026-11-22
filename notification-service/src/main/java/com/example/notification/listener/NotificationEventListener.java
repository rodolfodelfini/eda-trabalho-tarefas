package com.example.notification.listener;

import com.example.notification.service.NotificationService;
import com.example.task.event.TaskCreatedEvent;
import com.example.task.event.TaskUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${rabbitmq.queue.notification}")
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitHandler
    public void handleTaskCreated(TaskCreatedEvent event) {
        log.info("Notification Service recebeu TASK_CREATED: ID {}", event.getTaskId());
        notificationService.sendNotification(
                event.getTaskId(),
                event.getTitle(),
                event.getAssignee(),
                event.getPriority()
        );
    }

    @RabbitHandler
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        log.info("Notification Service recebeu TASK_UPDATED: ID {}", event.getTaskId());
        notificationService.sendUpdateNotification(
                event.getTaskId(),
                event.getTitle(),
                event.getAssignee(),
                event.getPriority(),
                event.getStatus()
        );
    }
}
