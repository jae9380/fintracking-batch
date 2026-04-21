package com.ft.batch.domain;

import com.ft.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 7)
    private String yearMonth; // "2026-04" 형식

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalIncome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalExpense;

    private Long categoryId; // 카테고리별 row의 경우 설정, 전체 집계 row는 null

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal categoryExpense;

    private MonthlyStatistics(Long userId, String yearMonth,
                              BigDecimal totalIncome, BigDecimal totalExpense,
                              Long categoryId, BigDecimal categoryExpense) {
        this.userId = userId;
        this.yearMonth = yearMonth;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.categoryId = categoryId;
        this.categoryExpense = categoryExpense;
    }

    public static MonthlyStatistics create(Long userId, String yearMonth,
                                           BigDecimal totalIncome, BigDecimal totalExpense,
                                           Long categoryId, BigDecimal categoryExpense) {
        return new MonthlyStatistics(userId, yearMonth, totalIncome, totalExpense, categoryId, categoryExpense);
    }
}
