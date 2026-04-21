package com.ft.batch.application.port;

import com.ft.batch.domain.MonthlyStatistics;

import java.util.List;

public interface MonthlyStatisticsRepository {

    void saveAll(List<MonthlyStatistics> statistics);
}
