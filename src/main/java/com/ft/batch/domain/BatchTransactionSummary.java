package com.ft.batch.domain;

import com.ft.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * transaction-service의 TransactionCreatedEvent를 수신해 batch-service가 자체 보관하는 거래 집계 데이터.
 * transaction-service DB를 직접 참조하지 않기 위한 로컬 복사본.
 * TRANSFER는 수입/지출 통계에 포함하지 않으므로 저장하지 않는다.
 */
@Entity
@Table(name = "batch_transaction_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchTransactionSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type;        // INCOME | EXPENSE

    private Long categoryId;    // EXPENSE만 카테고리 존재 가능

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 7)
    private String yearMonth;   // "2026-04" 형식

    private BatchTransactionSummary(Long userId, String type, Long categoryId,
                                    BigDecimal amount, String yearMonth) {
        this.userId = userId;
        this.type = type;
        this.categoryId = categoryId;
        this.amount = amount;
        this.yearMonth = yearMonth;
    }

    public static BatchTransactionSummary create(Long userId, String type, Long categoryId,
                                                 BigDecimal amount, String yearMonth) {
        return new BatchTransactionSummary(userId, type, categoryId, amount, yearMonth);
    }
}
