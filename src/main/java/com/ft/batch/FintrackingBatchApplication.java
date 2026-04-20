package com.ft.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FintrackingBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintrackingBatchApplication.class, args);
    }

}
