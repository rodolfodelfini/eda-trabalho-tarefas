package com.example.analytics.controller;

import com.example.analytics.model.TaskMetric;
import com.example.analytics.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<List<TaskMetric>> getAllMetrics() {
        return ResponseEntity.ok(analyticsService.getAllMetrics());
    }

    @GetMapping("/today")
    public ResponseEntity<TaskMetric> getTodayMetrics() {
        return analyticsService.getTodayMetrics()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
