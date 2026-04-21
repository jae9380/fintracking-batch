package com.ft.batch.application.decorator;

import com.ft.batch.application.BatchJobExecutor;
import com.ft.common.exception.CustomException;
import com.ft.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;

@Slf4j
public class RetryBatchDecorator extends AbstractBatchDecorator {

    private static final int MAX_RETRY = 3;

    public RetryBatchDecorator(BatchJobExecutor delegate) {
        super(delegate);
    }

    @Override
    public void execute(YearMonth yearMonth) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY) {
            attempt++;
            try {
                log.info("[Batch][Retry] 실행 시도 — yearMonth={}, attempt={}/{}", yearMonth, attempt, MAX_RETRY);
                delegate.execute(yearMonth);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("[Batch][Retry] 실행 실패 — yearMonth={}, attempt={}/{}, error={}",
                        yearMonth, attempt, MAX_RETRY, e.getMessage());
            }
        }

        log.error("[Batch][Retry] 최대 재시도 횟수 초과 — yearMonth={}, maxRetry={}", yearMonth, MAX_RETRY);
        throw new CustomException(ErrorCode.BATCH_MAX_RETRY_EXCEEDED);
    }
}
