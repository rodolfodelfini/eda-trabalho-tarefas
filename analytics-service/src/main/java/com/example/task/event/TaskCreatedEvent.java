package com.example.task.event;

public class TaskCreatedEvent extends TaskEvent {

    private String title;
    private String description;
    private String assignee;
    private String priority;

    public TaskCreatedEvent() {
    }

    public TaskCreatedEvent(Long taskId, String title, String description,
                            String assignee, String priority) {
        super("TASK_CREATED", taskId);
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.priority = priority;
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
}
