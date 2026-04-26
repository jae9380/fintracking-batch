package com.ft.batch.domain;

import com.ft.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * auth-service의 UserRegisteredEvent를 수신해 batch-service가 자체 보관하는 사용자 정보.
 * auth-service DB를 직접 참조하지 않기 위한 로컬 복사본.
 */
@Entity
@Table(name = "batch_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String email;

    private BatchUser(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public static BatchUser create(Long userId, String email) {
        return new BatchUser(userId, email);
    }
}
