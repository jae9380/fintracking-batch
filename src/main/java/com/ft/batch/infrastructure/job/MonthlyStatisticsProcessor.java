package com.ft.batch.infrastructure.job;

import com.ft.batch.application.port.BatchTransactionSummaryRepository;
import com.ft.batch.domain.BatchUser;
import com.ft.batch.domain.MonthlyStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * BatchUser 한 명에 대해 전월 트랜잭션을 집계하여 MonthlyStatistics 목록을 생성한다.
 * transaction-service DB에 직접 접근하지 않고 batch_transaction_summary 테이블을 사용한다.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyStatisticsProcessor implements ItemProcessor<BatchUser, List<MonthlyStatistics>> {

    private final BatchTransactionSummaryRepository batchTransactionSummaryRepository;

    @Value("#{jobParameters['yearMonth']}")
    private String yearMonthStr;

    @Override
    public List<MonthlyStatistics> process(BatchUser batchUser) {
        Long userId = batchUser.getUserId();

        BigDecimal totalIncome = batchTransactionSummaryRepository
                .sumAmountByUserIdAndTypeAndYearMonth(userId, "INCOME", yearMonthStr);
        BigDecimal totalExpense = batchTransactionSummaryRepository
                .sumAmountByUserIdAndTypeAndYearMonth(userId, "EXPENSE", yearMonthStr);

        List<Object[]> categoryRows = batchTransactionSummaryRepository
                .sumExpenseGroupedByCategoryAndYearMonth(userId, yearMonthStr);

        List<MonthlyStatistics> result = new ArrayList<>();

        if (categoryRows.isEmpty()) {
            result.add(MonthlyStatistics.create(
                    userId, yearMonthStr,
                    totalIncome, totalExpense,
                    null, BigDecimal.ZERO
            ));
        } else {
            for (Object[] row : categoryRows) {
                Long categoryId = (Long) row[0];
                BigDecimal categoryExpense = (BigDecimal) row[1];

                result.add(MonthlyStatistics.create(
                        userId, yearMonthStr,
                        totalIncome, totalExpense,
                        categoryId, categoryExpense
                ));
            }
        }

        log.debug("[Batch][Processor] 집계 완료 — userId={}, yearMonth={}, rows={}",
                userId, yearMonthStr, result.size());
        return result;
    }
}
