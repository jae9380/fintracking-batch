package com.ft.batch.infrastructure.persistence;

import com.ft.batch.domain.BatchUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaBatchUserRepository extends JpaRepository<BatchUser, Long> {

    boolean existsByUserId(Long userId);

    @Query("SELECT bu.userId FROM BatchUser bu ORDER BY bu.userId ASC")
    List<Long> findAllUserIds();
}
