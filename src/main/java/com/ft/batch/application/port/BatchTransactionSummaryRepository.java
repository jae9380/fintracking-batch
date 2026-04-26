package com.ft.batch.application.port;

import com.ft.batch.domain.BatchTransactionSummary;

import java.math.BigDecimal;
import java.util.List;

public interface BatchTransactionSummaryRepository {

    void save(BatchTransactionSummary summary);

    BigDecimal sumAmountByUserIdAndTypeAndYearMonth(Long userId, String type, String yearMonth);

    List<Object[]> sumExpenseGroupedByCategoryAndYearMonth(Long userId, String yearMonth);
}
