package com.ft.batch.temp;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Configuration
public class TimeLogJobConfig {

    @Bean
    public Job timeLogJob(JobRepository jobRepository, Step timeLogStep) {
        return new JobBuilder("timeLogJob", jobRepository)
                .start(timeLogStep)
                .build();
    }

    @Bean
    public Step timeLogStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("timeLogStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("[TimeLogJob] 현재 시간: " + LocalDateTime.now());
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
