package com.ft.batch.infrastructure.scheduler;

import com.ft.batch.application.BatchJobExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchJobExecutor batchJobExecutor;

    @Scheduled(cron = "0 0 1 1 * *")
    public void runMonthlyStatistics() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        log.info("[BatchScheduler] 월별 통계 배치 트리거 — targetMonth={}", lastMonth);
        batchJobExecutor.execute(lastMonth);
    }
}
