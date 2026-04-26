package com.ft.batch.application.decorator;

import com.ft.batch.application.BatchJobExecutor;
import com.ft.batch.application.BatchReportEventPublisher;
import com.ft.batch.application.port.BatchUserRepository;
import com.ft.common.event.BatchReportEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 배치 완료 후 batch.report 토픽에 BatchReportEvent를 발행한다.
 * notification-service가 해당 토픽을 구독하여 사용자에게 월간 리포트 알림을 발송한다.
 * auth-service, notification-service를 직접 참조하지 않는다.
 */
@Slf4j
public class NotificationBatchDecorator extends AbstractBatchDecorator {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월");

    private final BatchUserRepository batchUserRepository;
    private final BatchReportEventPublisher batchReportEventPublisher;

    public NotificationBatchDecorator(BatchJobExecutor delegate,
                                      BatchUserRepository batchUserRepository,
                                      BatchReportEventPublisher batchReportEventPublisher) {
        super(delegate);
        this.batchUserRepository = batchUserRepository;
        this.batchReportEventPublisher = batchReportEventPublisher;
    }

    @Override
    public void execute(YearMonth yearMonth) {
        delegate.execute(yearMonth);

        String yearMonthStr = yearMonth.toString();
        String displayMonth = yearMonth.format(DISPLAY_FORMATTER);
        String title = displayMonth + " 월간 리포트";
        String message = displayMonth + " 지출/수입 통계가 집계되었습니다. 앱에서 확인해 보세요.";

        List<Long> userIds = batchUserRepository.findAllUserIds();

        for (Long userId : userIds) {
            try {
                batchReportEventPublisher.publish(new BatchReportEvent(
                        UUID.randomUUID().toString(),
                        userId,
                        yearMonthStr,
                        title,
                        message
                ));
            } catch (Exception e) {
                log.error("[Batch][Notification] 리포트 이벤트 발행 실패 — userId={}, error={}", userId, e.getMessage());
            }
        }

        log.info("[Batch][Notification] 월간 리포트 이벤트 발행 완료 — yearMonth={}, userCount={}",
                yearMonth, userIds.size());
    }
}
