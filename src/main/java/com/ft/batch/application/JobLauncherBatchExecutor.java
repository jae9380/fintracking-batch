package com.ft.batch.application;

import com.ft.common.exception.CustomException;
import com.ft.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Spring Batch JobLauncher를 감싸는 실제 실행 구현체.
 * Decorator 체인의 가장 안쪽에 위치한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLauncherBatchExecutor implements BatchJobExecutor {

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final JobLauncher jobLauncher;
    private final Job monthlyStatisticsJob;

    @Override
    public void execute(YearMonth yearMonth) {
        String yearMonthStr = yearMonth.format(YEAR_MONTH_FORMATTER);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("yearMonth", yearMonthStr)
                .addLong("runAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(monthlyStatisticsJob, jobParameters);
        } catch (Exception e) {
            log.error("[Batch] Job 실행 실패 — yearMonth={}, error={}", yearMonthStr, e.getMessage());
            throw new CustomException(ErrorCode.BATCH_JOB_EXECUTION_FAILED);
        }
    }
}
