package com.ft.batch.infrastructure.scheduler;

import com.ft.auth.infrastructure.persistence.JpaUserRepository;
import com.ft.batch.application.BatchJobExecutor;
import com.ft.batch.application.JobLauncherBatchExecutor;
import com.ft.batch.application.decorator.LoggingBatchDecorator;
import com.ft.batch.application.decorator.NotificationBatchDecorator;
import com.ft.batch.application.decorator.RetryBatchDecorator;
import com.ft.notification.application.NotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {
    /* note: 배치 설계 구조 관련 코멘트
         LoggingDecorator
          └── NotificationDecorator
                  └── RetryDecorator
                          └── JobLauncherBatchExecutor
        배치 구조에 데코레이터를 사용하여 아래와 같은 기능을 추가
        1. 실행 시간 측정 및 로깅
        2. 실패 시 재시도
        3. 성공 시 알림 발송
        위 구조의 순서가 중요한 이유로는
          Logging (바깥)
          └── Notification (중간)
                  └── Retry (안쪽)
                          └── 실제 Job
        이와 같은 구조가 되어야 Logging이 전체 실행 시간을 측적하게 되고, 알림이 재실행마다 발송하지 않고 전체 실행에 하나의 알림만 발송하게 된다.


        이 같은 구조에서 추가적인 기능이 생기면 기존 코드는 수정할 필요가 없는 형태가 된다. OCP
        또한, 독립적인 테스트가 가능하다.
     */
    @Bean
    public BatchJobExecutor batchJobExecutor(
            JobLauncherBatchExecutor jobLauncherBatchExecutor,
            JpaUserRepository jpaUserRepository,
            NotificationService notificationService
    ) {
        BatchJobExecutor retryDecorator = new RetryBatchDecorator(jobLauncherBatchExecutor);
        BatchJobExecutor notificationDecorator = new NotificationBatchDecorator(
                retryDecorator, jpaUserRepository, notificationService);
        return new LoggingBatchDecorator(notificationDecorator);
    }
}
