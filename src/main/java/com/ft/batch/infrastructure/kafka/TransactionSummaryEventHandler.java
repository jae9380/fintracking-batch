package com.ft.batch.infrastructure.kafka;

import com.ft.batch.application.port.BatchTransactionSummaryRepository;
import com.ft.batch.domain.BatchTransactionSummary;
import com.ft.common.event.TransactionCreatedEvent;
import com.ft.common.kafka.EventHandler;
import com.ft.common.kafka.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionSummaryEventHandler implements EventHandler<TransactionCreatedEvent> {

    private final BatchTransactionSummaryRepository batchTransactionSummaryRepository;

    @KafkaListener(topics = KafkaTopic.TRANSACTION_CREATED, groupId = "batch-service")
    @Override
    public void handle(TransactionCreatedEvent event) {
        // TRANSFER는 수입/지출 통계 대상이 아니므로 저장하지 않음
        if ("TRANSFER".equals(event.type())) {
            return;
        }

        String yearMonth = YearMonth.from(event.transactedAt()).toString();

        batchTransactionSummaryRepository.save(
                BatchTransactionSummary.create(
                        event.userId(),
                        event.type(),
                        event.categoryId(),
                        event.amount(),
                        yearMonth
                )
        );

        log.debug("[Batch] BatchTransactionSummary 저장 — userId={}, type={}, amount={}, yearMonth={}",
                event.userId(), event.type(), event.amount(), yearMonth);
    }
}
