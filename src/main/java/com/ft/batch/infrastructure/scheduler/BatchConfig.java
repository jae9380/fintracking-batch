package com.ft.batch.infrastructure.scheduler;

import com.ft.batch.application.BatchJobExecutor;
import com.ft.batch.application.BatchReportEventPublisher;
import com.ft.batch.application.JobLauncherBatchExecutor;
import com.ft.batch.application.decorator.LoggingBatchDecorator;
import com.ft.batch.application.decorator.MetricBatchDecorator;
import com.ft.batch.application.decorator.NotificationBatchDecorator;
import com.ft.batch.application.decorator.RetryBatchDecorator;
import com.ft.batch.application.port.BatchUserRepository;
import com.ft.common.metric.helper.BatchMetricHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Decorator 체인 조립.
 *
 * MetricDecorator    (최외곽 — 메트릭 수집)
 *   └── LoggingDecorator    (전체 실행 시간 로깅)
 *         └── NotificationDecorator (배치 완료 후 BatchReportEvent 발행)
 *               └── RetryDecorator  (실패 시 최대 3회 재시도)
 *                     └── JobLauncherBatchExecutor (실제 Job 실행)
 */
@Configuration
public class BatchConfig {

    @Bean
    public BatchJobExecutor batchJobExecutor(
            JobLauncherBatchExecutor jobLauncherBatchExecutor,
            BatchUserRepository batchUserRepository,
            BatchReportEventPublisher batchReportEventPublisher,
            BatchMetricHelper batchMetricHelper
    ) {
        BatchJobExecutor retryDecorator = new RetryBatchDecorator(jobLauncherBatchExecutor);
        BatchJobExecutor notificationDecorator = new NotificationBatchDecorator(
                retryDecorator, batchUserRepository, batchReportEventPublisher);
        BatchJobExecutor loggingDecorator = new LoggingBatchDecorator(notificationDecorator);
        return new MetricBatchDecorator(loggingDecorator, batchMetricHelper);
    }
}
