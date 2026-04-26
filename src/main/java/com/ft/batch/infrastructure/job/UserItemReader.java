package com.ft.batch.infrastructure.job;

import com.ft.batch.domain.BatchUser;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

/**
 * batch_users 테이블에서 BatchUser를 페이지 단위로 읽는다.
 * auth-service의 User 클래스나 DB에 직접 접근하지 않는다.
 * UserRegisteredEventHandler가 사전에 BatchUser를 적재해 두어야 한다.
 */
@Component
@RequiredArgsConstructor
public class UserItemReader {

    private static final int PAGE_SIZE = 10;

    private final EntityManagerFactory entityManagerFactory;

    public JpaPagingItemReader<BatchUser> create() {
        return new JpaPagingItemReaderBuilder<BatchUser>()
                .name("batchUserItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT bu FROM BatchUser bu ORDER BY bu.userId ASC")
                .pageSize(PAGE_SIZE)
                .build();
    }
}
