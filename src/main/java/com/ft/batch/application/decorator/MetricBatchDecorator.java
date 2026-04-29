package com.ft.batch.application.decorator;

import com.ft.batch.application.BatchJobExecutor;
import com.ft.common.metric.helper.BatchMetricHelper;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;

/**
 * 배치 Job 실행 메트릭 수집 데코레이터 (최외곽).
 *
 * <pre>
 * 수집 메트릭:
 *   ft_batch_job_total{job="monthlyStatisticsJob", result="success"|"fail"}
 *   ft_batch_job_duration_seconds{job="monthlyStatisticsJob", result}
 * </pre>
 *
 * 데코레이터 체인 순서:
 * MetricDecorator → LoggingDecorator → NotificationDecorator → RetryDecorator → JobLauncher
 */
@Slf4j
public class MetricBatchDecorator extends AbstractBatchDecorator {

    private static final String JOB_NAME = "monthlyStatisticsJob";

    private final BatchMetricHelper metricHelper;

    public MetricBatchDecorator(BatchJobExecutor delegate, BatchMetricHelper metricHelper) {
        super(delegate);
        this.metricHelper = metricHelper;
    }

    @Override
    public void execute(YearMonth yearMonth) {
        long startMs = System.currentTimeMillis();
        String result = "success";

        try {
            delegate.execute(yearMonth);
            metricHelper.success(JOB_NAME);

        } catch (Exception e) {
            result = "fail";
            metricHelper.fail(JOB_NAME, e.getClass().getSimpleName());
            throw e;

        } finally {
            long elapsedMs = System.currentTimeMillis() - startMs;
            metricHelper.recordDuration(JOB_NAME, result, elapsedMs);
        }
    }
}
