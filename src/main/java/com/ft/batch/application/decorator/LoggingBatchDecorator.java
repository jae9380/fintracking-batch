package com.ft.batch.application.decorator;

import com.ft.batch.application.BatchJobExecutor;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;

@Slf4j
public class LoggingBatchDecorator extends AbstractBatchDecorator {

    public LoggingBatchDecorator(BatchJobExecutor delegate) {
        super(delegate);
    }

    @Override
    public void execute(YearMonth yearMonth) {
        long startMillis = System.currentTimeMillis();
        log.info("[Batch][Logging] 실행 시작 — yearMonth={}", yearMonth);

        try {
            delegate.execute(yearMonth);
            long elapsedMs = System.currentTimeMillis() - startMillis;
            log.info("[Batch][Logging] 실행 완료 — yearMonth={}, elapsedMs={}", yearMonth, elapsedMs);
        } catch (Exception e) {
            long elapsedMs = System.currentTimeMillis() - startMillis;
            log.error("[Batch][Logging] 실행 실패 — yearMonth={}, elapsedMs={}, error={}",
                    yearMonth, elapsedMs, e.getMessage());
            throw e;
        }
    }
}
