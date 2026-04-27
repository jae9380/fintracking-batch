package com.ft.batch.infrastructure.kafka;

import com.ft.batch.application.port.BatchTransactionSummaryRepository;
import com.ft.batch.domain.BatchTransactionSummary;
import com.ft.common.event.TransactionCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionSummaryEventHandler 단위 테스트")
class TransactionSummaryEventHandlerTest {

    @Mock BatchTransactionSummaryRepository batchTransactionSummaryRepository;
    @InjectMocks TransactionSummaryEventHandler handler;

    private TransactionCreatedEvent event(String type) {
        return new TransactionCreatedEvent(
                "evt-001", 1L, 10L, null, 100L,
                new BigDecimal("5000"), type,
                5L, "식비", LocalDateTime.of(2026, 4, 1, 12, 0)
        );
    }

    @Nested
    @DisplayName("거래 유형별 집계 저장")
    class Handle {

        @Test
        @DisplayName("성공 - INCOME 거래는 집계 테이블에 저장된다")
        void handle_whenIncome_savesSummary() {
            // given
            TransactionCreatedEvent incomeEvent = event("INCOME");

            // when
            handler.handle(incomeEvent);

            // then
            then(batchTransactionSummaryRepository).should()
                    .save(argThat(s -> "INCOME".equals(s.getType()) && s.getUserId().equals(1L)));
        }

        @Test
        @DisplayName("성공 - EXPENSE 거래는 집계 테이블에 저장된다")
        void handle_whenExpense_savesSummary() {
            // given
            TransactionCreatedEvent expenseEvent = event("EXPENSE");

            // when
            handler.handle(expenseEvent);

            // then
            then(batchTransactionSummaryRepository).should()
                    .save(argThat(s -> "EXPENSE".equals(s.getType()) && s.getUserId().equals(1L)));
        }

        @Test
        @DisplayName("성공 - TRANSFER 거래는 저장하지 않는다")
        void handle_whenTransfer_skipsAndDoesNotSave() {
            // given
            TransactionCreatedEvent transferEvent = new TransactionCreatedEvent(
                    "evt-002", 1L, 10L, 20L, 101L,
                    new BigDecimal("10000"), "TRANSFER",
                    null, null, LocalDateTime.of(2026, 4, 1, 13, 0)
            );

            // when
            handler.handle(transferEvent);

            // then
            then(batchTransactionSummaryRepository).should(never()).save(any(BatchTransactionSummary.class));
        }
    }
}
