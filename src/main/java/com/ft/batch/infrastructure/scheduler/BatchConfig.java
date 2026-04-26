package com.ft.batch.infrastructure.scheduler;

import com.ft.batch.application.BatchJobExecutor;
import com.ft.batch.application.BatchReportEventPublisher;
import com.ft.batch.application.JobLauncherBatchExecutor;
import com.ft.batch.application.decorator.LoggingBatchDecorator;
import com.ft.batch.application.decorator.NotificationBatchDecorator;
import com.ft.batch.application.decorator.RetryBatchDecorator;
import com.ft.batch.application.port.BatchUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Decorator 체인 조립.
 *
 * LoggingDecorator (최외곽 — 전체 실행 시간 측정)
 *   └── NotificationDecorator (배치 완료 후 BatchReportEvent 발행)
 *         └── RetryDecorator (실패 시 최대 3회 재시도)
 *               └── JobLauncherBatchExecutor (실제 Job 실행)
 */
@Configuration
public class BatchConfig {

    @Bean
    public BatchJobExecutor batchJobExecutor(
            JobLauncherBatchExecutor jobLauncherBatchExecutor,
            BatchUserRepository batchUserRepository,
            BatchReportEventPublisher batchReportEventPublisher
    ) {
        BatchJobExecutor retryDecorator = new RetryBatchDecorator(jobLauncherBatchExecutor);
        BatchJobExecutor notificationDecorator = new NotificationBatchDecorator(
                retryDecorator, batchUserRepository, batchReportEventPublisher);
        return new LoggingBatchDecorator(notificationDecorator);
    }
}
