package com.example.analytics.service;

import com.example.analytics.model.ProcessedEvent;
import com.example.analytics.model.TaskMetric;
import com.example.analytics.repository.ProcessedEventRepository;
import com.example.analytics.repository.TaskMetricRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final TaskMetricRepository metricRepository;
    private final ProcessedEventRepository processedEventRepository;

    public AnalyticsService(TaskMetricRepository metricRepository, ProcessedEventRepository processedEventRepository) {
        this.metricRepository = metricRepository;
        this.processedEventRepository = processedEventRepository;
    }

    // Metrica e eventId sao gravados na mesma transacao: ou as duas coisas acontecem, ou nenhuma
    @Transactional
    public void recordTaskCreated(String eventId, Long taskId, String priority) {
        if (eventId != null && processedEventRepository.existsById(eventId)) {
            log.warn("Evento {} ja processado anteriormente (tarefa {}), ignorando", eventId, taskId);
            return;
        }

        LocalDate today = LocalDate.now();
        TaskMetric metric = metricRepository.findByDate(today).orElseGet(() -> {
            TaskMetric m = new TaskMetric();
            m.setDate(today);
            return m;
        });

        metric.setTotalTasks(plusOne(metric.getTotalTasks()));

        String p = priority != null ? priority.toUpperCase() : "MEDIUM";
        switch (p) {
            case "CRITICAL" -> metric.setCriticalPriorityTasks(plusOne(metric.getCriticalPriorityTasks()));
            case "HIGH" -> metric.setHighPriorityTasks(plusOne(metric.getHighPriorityTasks()));
            case "LOW" -> metric.setLowPriorityTasks(plusOne(metric.getLowPriorityTasks()));
            default -> metric.setMediumPriorityTasks(plusOne(metric.getMediumPriorityTasks()));
        }

        metricRepository.save(metric);

        if (eventId != null) {
            processedEventRepository.save(new ProcessedEvent(eventId, "TASK_CREATED", taskId));
        }

        log.info("Metrica atualizada: {} | Total: {} | Criticas: {}",
                today, metric.getTotalTasks(), metric.getCriticalPriorityTasks());
    }

    public Optional<TaskMetric> getTodayMetrics() {
        return metricRepository.findByDate(LocalDate.now());
    }

    public List<TaskMetric> getAllMetrics() {
        return metricRepository.findAll();
    }

    private static long plusOne(Long value) {
        return value == null ? 1 : value + 1;
    }
}
