package com.example.analytics.repository;

import com.example.analytics.model.TaskMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TaskMetricRepository extends JpaRepository<TaskMetric, Long> {
    Optional<TaskMetric> findByDate(LocalDate date);
}
