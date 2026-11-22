package com.example.task.event;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class TaskEvent {

    private String eventId;
    private String eventType;
    private Long taskId;
    private LocalDateTime timestamp;

    public TaskEvent() {
    }

    public TaskEvent(String eventType, Long taskId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.taskId = taskId;
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
