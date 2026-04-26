package com.ft.batch.infrastructure.persistence;

import com.ft.batch.application.port.BatchTransactionSummaryRepository;
import com.ft.batch.domain.BatchTransactionSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchTransactionSummaryRepositoryImpl implements BatchTransactionSummaryRepository {

    private final JpaBatchTransactionSummaryRepository jpaRepository;

    @Override
    public void save(BatchTransactionSummary summary) {
        jpaRepository.save(summary);
    }

    @Override
    public BigDecimal sumAmountByUserIdAndTypeAndYearMonth(Long userId, String type, String yearMonth) {
        return jpaRepository.sumAmountByUserIdAndTypeAndYearMonth(userId, type, yearMonth);
    }

    @Override
    public List<Object[]> sumExpenseGroupedByCategoryAndYearMonth(Long userId, String yearMonth) {
        return jpaRepository.sumExpenseGroupedByCategoryAndYearMonth(userId, yearMonth);
    }
}
