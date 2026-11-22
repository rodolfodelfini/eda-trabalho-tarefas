package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification sendNotification(Long taskId, String taskTitle, String assignee, String priority) {
        String message = String.format("Nova tarefa: '%s' (Prioridade: %s) para %s",
                taskTitle, priority, assignee != null ? assignee : "Nao atribuido");
        return register(taskId, taskTitle, assignee, priority, message);
    }

    public Notification sendUpdateNotification(Long taskId, String taskTitle, String assignee,
                                               String priority, String status) {
        String message = String.format("Tarefa atualizada: '%s' (Status: %s, Prioridade: %s)",
                taskTitle, status, priority);
        return register(taskId, taskTitle, assignee, priority, message);
    }

    // O envio e simulado: so registra no log e grava o historico no banco
    private Notification register(Long taskId, String taskTitle, String assignee,
                                  String priority, String message) {
        String channel = "CRITICAL".equalsIgnoreCase(priority) ? "SMS" : "EMAIL";
        log.info("[NOTIFICACAO] Canal: {} | Mensagem: {}", channel, message);

        Notification notif = new Notification();
        notif.setTaskId(taskId);
        notif.setTaskTitle(taskTitle);
        notif.setRecipient(assignee);
        notif.setChannel(channel);
        notif.setMessage(message);
        notif.setSentAt(LocalDateTime.now());
        notif.setSuccess(true);

        Notification saved = notificationRepository.save(notif);
        log.info("Notificacao registrada: ID {} (tarefa {})", saved.getId(), taskId);
        return saved;
    }
}
