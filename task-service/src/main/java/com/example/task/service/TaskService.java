package com.example.task.service;

import com.example.task.event.TaskCreatedEvent;
import com.example.task.event.TaskUpdatedEvent;
import com.example.task.model.Task;
import com.example.task.model.TaskPriority;
import com.example.task.model.TaskStatus;
import com.example.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.created}")
    private String createdRoutingKey;

    @Value("${rabbitmq.routing.updated:task.updated}")
    private String updatedRoutingKey;

    public TaskService(TaskRepository taskRepository, RabbitTemplate rabbitTemplate) {
        this.taskRepository = taskRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Task createTask(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Título da tarefa é obrigatório");
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        Task savedTask = taskRepository.save(task);
        log.info("Tarefa criada: ID {} - {}", savedTask.getId(), savedTask.getTitle());

        TaskCreatedEvent event = new TaskCreatedEvent(
                savedTask.getId(),
                savedTask.getTitle(),
                savedTask.getDescription(),
                savedTask.getAssignee(),
                savedTask.getPriority().name()
        );

        rabbitTemplate.convertAndSend(exchangeName, createdRoutingKey, event);
        log.info("Evento TASK_CREATED publicado para tarefa {}", savedTask.getId());

        return savedTask;
    }

    @Transactional
    public Optional<Task> updateTask(Long id, Task taskUpdates) {
        return taskRepository.findById(id).map(existing -> {
            if (taskUpdates.getTitle() != null && !taskUpdates.getTitle().trim().isEmpty()) {
                existing.setTitle(taskUpdates.getTitle());
            }
            if (taskUpdates.getDescription() != null) {
                existing.setDescription(taskUpdates.getDescription());
            }
            if (taskUpdates.getAssignee() != null) {
                existing.setAssignee(taskUpdates.getAssignee());
            }
            if (taskUpdates.getPriority() != null) {
                existing.setPriority(taskUpdates.getPriority());
            }
            if (taskUpdates.getStatus() != null) {
                existing.setStatus(taskUpdates.getStatus());
            }
            existing.setUpdatedAt(LocalDateTime.now());

            Task updatedTask = taskRepository.save(existing);
            log.info("Tarefa atualizada: ID {} - Status {}", updatedTask.getId(), updatedTask.getStatus());

            TaskUpdatedEvent event = new TaskUpdatedEvent(
                    updatedTask.getId(),
                    updatedTask.getTitle(),
                    updatedTask.getDescription(),
                    updatedTask.getAssignee(),
                    updatedTask.getPriority().name(),
                    updatedTask.getStatus().name()
            );

            rabbitTemplate.convertAndSend(exchangeName, updatedRoutingKey, event);
            log.info("Evento TASK_UPDATED publicado para tarefa {}", updatedTask.getId());

            return updatedTask;
        });
    }
}
