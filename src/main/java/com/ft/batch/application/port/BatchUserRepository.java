package com.ft.batch.application.port;

import com.ft.batch.domain.BatchUser;

import java.util.List;

public interface BatchUserRepository {

    BatchUser save(BatchUser batchUser);

    boolean existsByUserId(Long userId);

    List<Long> findAllUserIds();
}
