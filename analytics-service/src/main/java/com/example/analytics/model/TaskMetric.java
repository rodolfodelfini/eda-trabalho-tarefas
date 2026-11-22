package com.example.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "task_metrics")
public class TaskMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate date;

    private Long totalTasks;
    private Long highPriorityTasks;
    private Long mediumPriorityTasks;
    private Long lowPriorityTasks;
    private Long criticalPriorityTasks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Long getHighPriorityTasks() {
        return highPriorityTasks;
    }

    public void setHighPriorityTasks(Long highPriorityTasks) {
        this.highPriorityTasks = highPriorityTasks;
    }

    public Long getMediumPriorityTasks() {
        return mediumPriorityTasks;
    }

    public void setMediumPriorityTasks(Long mediumPriorityTasks) {
        this.mediumPriorityTasks = mediumPriorityTasks;
    }

    public Long getLowPriorityTasks() {
        return lowPriorityTasks;
    }

    public void setLowPriorityTasks(Long lowPriorityTasks) {
        this.lowPriorityTasks = lowPriorityTasks;
    }

    public Long getCriticalPriorityTasks() {
        return criticalPriorityTasks;
    }

    public void setCriticalPriorityTasks(Long criticalPriorityTasks) {
        this.criticalPriorityTasks = criticalPriorityTasks;
    }
}
