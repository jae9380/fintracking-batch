package com.ft.batch.application.decorator;

// TODO: [Kafka 도입 시] JpaUserRepository 제거
// auth-service의 JPA 레포지토리를 batch-service가 직접 참조 — MSA 원칙 위반
// userId 목록은 auth-service REST API 호출 또는 별도 user-id 목록 Kafka 이벤트로 수신
import com.ft.auth.infrastructure.persistence.JpaUserRepository;
import com.ft.batch.application.BatchJobExecutor;
// TODO: [Kafka 도입 시] NotificationService, NotificationType 제거
// notification-service를 batch-service가 직접 호출 — MSA 원칙 위반
// 알림 발송은 kafkaTemplate.send("batch.report", BatchReportEvent)로 대체
// notification-service가 해당 토픽을 구독하여 처리
import com.ft.notification.application.NotificationService;
import com.ft.notification.domain.NotificationType;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class NotificationBatchDecorator extends AbstractBatchDecorator {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월");

    // TODO: [Kafka 도입 시] JpaUserRepository 제거 — auth-service DB 직접 접근
    private final JpaUserRepository jpaUserRepository;
    // TODO: [Kafka 도입 시] NotificationService 직접 호출 제거
    // KafkaTemplate으로 "batch.report" 토픽에 BatchReportEvent 발행으로 대체
    private final NotificationService notificationService;

    public NotificationBatchDecorator(BatchJobExecutor delegate,
                                      JpaUserRepository jpaUserRepository,
                                      NotificationService notificationService) {
        super(delegate);
        this.jpaUserRepository = jpaUserRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void execute(YearMonth yearMonth) {
        delegate.execute(yearMonth);

        String displayMonth = yearMonth.format(DISPLAY_FORMATTER);
        String title = displayMonth + " 월간 리포트";
        String message = displayMonth + " 지출/수입 통계가 집계되었습니다. 앱에서 확인해 보세요.";

        // TODO: [Kafka 도입 시] jpaUserRepository.findAll() 제거
        // auth-service DB에 직접 접근하여 userId 목록 조회 — MSA 원칙 위반
        // 대안 1: batch 완료 이벤트 발행 후 notification-service가 자체 userId 목록으로 발송
        // 대안 2: auth-service에서 주기적으로 userId 목록을 Kafka로 동기화
        List<Long> userIds = jpaUserRepository.findAll()
                .stream()
                .map(user -> user.getId())
                .toList();

        for (Long userId : userIds) {
            try {
                // TODO: [Kafka 도입 시] notificationService.send() 직접 호출 제거
                // kafkaTemplate.send("batch.report", new BatchReportEvent(userId, title, message));
                notificationService.send(userId, NotificationType.MONTHLY_REPORT, title, message);
            } catch (Exception e) {
                log.error("[Batch][Notification] 알림 발송 실패 — userId={}, error={}", userId, e.getMessage());
            }
        }

        log.info("[Batch][Notification] 월간 리포트 알림 발송 완료 — yearMonth={}, userCount={}",
                yearMonth, userIds.size());
    }
}
