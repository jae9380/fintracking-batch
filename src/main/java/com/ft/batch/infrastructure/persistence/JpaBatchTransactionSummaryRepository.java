package com.ft.batch.infrastructure.persistence;

import com.ft.batch.domain.BatchTransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JpaBatchTransactionSummaryRepository extends JpaRepository<BatchTransactionSummary, Long> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM BatchTransactionSummary t " +
           "WHERE t.userId = :userId AND t.type = :type AND t.yearMonth = :yearMonth")
    BigDecimal sumAmountByUserIdAndTypeAndYearMonth(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("yearMonth") String yearMonth
    );

    @Query("SELECT t.categoryId, SUM(t.amount) FROM BatchTransactionSummary t " +
           "WHERE t.userId = :userId AND t.type = 'EXPENSE' AND t.yearMonth = :yearMonth " +
           "AND t.categoryId IS NOT NULL " +
           "GROUP BY t.categoryId")
    List<Object[]> sumExpenseGroupedByCategoryAndYearMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );
}
