package com.ft.batch.infrastructure.job;

import com.ft.batch.domain.BatchUser;
import com.ft.batch.domain.MonthlyStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Spring Batch 5.x Job/Step 설정.
 *
 * 청크 흐름:
 *   UserItemReader (JpaPagingItemReader<BatchUser>, chunk=10)
 *     → MonthlyStatisticsProcessor (BatchUser → List<MonthlyStatistics>)
 *     → MonthlyStatisticsWriter (flatten → saveAll)
 */
@Configuration
@RequiredArgsConstructor
public class MonthlyStatisticsJobConfig {

    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserItemReader userItemReader;
    private final MonthlyStatisticsProcessor monthlyStatisticsProcessor;
    private final MonthlyStatisticsWriter monthlyStatisticsWriter;

    @Bean
    public Job monthlyStatisticsJob() {
        return new JobBuilder("monthlyStatisticsJob", jobRepository)
                .start(monthlyStatisticsStep())
                .build();
    }

    @Bean
    public Step monthlyStatisticsStep() {
        return new StepBuilder("monthlyStatisticsStep", jobRepository)
                .<BatchUser, List<MonthlyStatistics>>chunk(CHUNK_SIZE, transactionManager)
                .reader(userItemReader.create())
                .processor(monthlyStatisticsProcessor)
                .writer(monthlyStatisticsWriter)
                .build();
    }
}
