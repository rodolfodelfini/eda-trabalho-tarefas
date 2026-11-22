package com.example.task.event;

public class TaskUpdatedEvent extends TaskEvent {

    private String title;
    private String description;
    private String assignee;
    private String priority;
    private String status;

    public TaskUpdatedEvent() {
    }

    public TaskUpdatedEvent(Long taskId, String title, String description,
                            String assignee, String priority, String status) {
        super("TASK_UPDATED", taskId);
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.priority = priority;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
