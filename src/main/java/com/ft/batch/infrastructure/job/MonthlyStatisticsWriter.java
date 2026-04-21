package com.ft.batch.infrastructure.job;

import com.ft.batch.application.port.MonthlyStatisticsRepository;
import com.ft.batch.domain.MonthlyStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyStatisticsWriter implements ItemWriter<List<MonthlyStatistics>> {

    private final MonthlyStatisticsRepository monthlyStatisticsRepository;

    @Override
    public void write(Chunk<? extends List<MonthlyStatistics>> chunk) {
        List<MonthlyStatistics> flatList = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();

        monthlyStatisticsRepository.saveAll(flatList);

        log.info("[Batch][Writer] MonthlyStatistics 저장 완료 — savedRows={}", flatList.size());
    }
}
