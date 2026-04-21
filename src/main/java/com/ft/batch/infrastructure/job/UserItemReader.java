package com.ft.batch.infrastructure.job;

// TODO: [Kafka 도입 시] auth-service의 User 도메인 클래스 직접 참조 제거
// auth-service DB를 batch-service가 직접 읽는 구조 — MSA 원칙 위반
// 대안: auth-service가 userId 목록을 제공하는 API 또는 Kafka 이벤트 활용
// UserItemReader 자체를 BatchUserItemReader로 교체하고 userId 기반으로 동작하도록 전환
import com.ft.auth.domain.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserItemReader {

    private static final int PAGE_SIZE = 10;

    private final EntityManagerFactory entityManagerFactory;

    // TODO: [Kafka 도입 시] User → BatchUser로 교체
    // "SELECT u FROM User u" 쿼리는 auth-service의 users 테이블에 의존
    // batch-service 전용 users 테이블 또는 userId 목록 API로 대체
    public JpaPagingItemReader<User> create() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u ORDER BY u.id ASC")
                .pageSize(PAGE_SIZE)
                .build();
    }
}
