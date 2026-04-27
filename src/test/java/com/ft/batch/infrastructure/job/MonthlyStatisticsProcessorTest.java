package com.ft.batch.infrastructure.job;

import com.ft.batch.application.port.BatchTransactionSummaryRepository;
import com.ft.batch.domain.BatchUser;
import com.ft.batch.domain.MonthlyStatistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonthlyStatisticsProcessor 단위 테스트")
class MonthlyStatisticsProcessorTest {

    @Mock BatchTransactionSummaryRepository batchTransactionSummaryRepository;

    MonthlyStatisticsProcessor processor;

    private static final String YEAR_MONTH = "2026-04";
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        processor = new MonthlyStatisticsProcessor(batchTransactionSummaryRepository);
        ReflectionTestUtils.setField(processor, "yearMonthStr", YEAR_MONTH);
    }

    private BatchUser batchUser() {
        return BatchUser.create(USER_ID, "user@example.com");
    }

    @Nested
    @DisplayName("월간 통계 집계")
    class Process {

        @Test
        @DisplayName("성공 - 카테고리 데이터가 없으면 전체 합산 단건을 반환한다")
        void process_whenNoCategoryRows_returnsSingleSummary() throws Exception {
            // given
            given(batchTransactionSummaryRepository.sumAmountByUserIdAndTypeAndYearMonth(USER_ID, "INCOME", YEAR_MONTH))
                    .willReturn(new BigDecimal("500000"));
            given(batchTransactionSummaryRepository.sumAmountByUserIdAndTypeAndYearMonth(USER_ID, "EXPENSE", YEAR_MONTH))
                    .willReturn(new BigDecimal("300000"));
            given(batchTransactionSummaryRepository.sumExpenseGroupedByCategoryAndYearMonth(USER_ID, YEAR_MONTH))
                    .willReturn(List.of());

            // when
            List<MonthlyStatistics> result = processor.process(batchUser());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTotalIncome()).isEqualByComparingTo(new BigDecimal("500000"));
            assertThat(result.get(0).getTotalExpense()).isEqualByComparingTo(new BigDecimal("300000"));
            assertThat(result.get(0).getCategoryId()).isNull();
        }

        @Test
        @DisplayName("성공 - 카테고리별 지출이 있으면 카테고리 수만큼 통계를 반환한다")
        void process_whenCategoryRowsExist_returnsOneStatisticsPerCategory() throws Exception {
            // given
            given(batchTransactionSummaryRepository.sumAmountByUserIdAndTypeAndYearMonth(USER_ID, "INCOME", YEAR_MONTH))
                    .willReturn(new BigDecimal("500000"));
            given(batchTransactionSummaryRepository.sumAmountByUserIdAndTypeAndYearMonth(USER_ID, "EXPENSE", YEAR_MONTH))
                    .willReturn(new BigDecimal("300000"));
            given(batchTransactionSummaryRepository.sumExpenseGroupedByCategoryAndYearMonth(USER_ID, YEAR_MONTH))
                    .willReturn(List.of(
                            new Object[]{5L, new BigDecimal("200000")},
                            new Object[]{6L, new BigDecimal("100000")}
                    ));

            // when
            List<MonthlyStatistics> result = processor.process(batchUser());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(s -> s.getTotalIncome().compareTo(new BigDecimal("500000")) == 0);
            assertThat(result.get(0).getCategoryId()).isEqualTo(5L);
            assertThat(result.get(1).getCategoryId()).isEqualTo(6L);
        }
    }
}
