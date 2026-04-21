package com.ft.batch.infrastructure.persistence;

import com.ft.batch.domain.MonthlyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMonthlyStatisticsRepository extends JpaRepository<MonthlyStatistics, Long> {
}
