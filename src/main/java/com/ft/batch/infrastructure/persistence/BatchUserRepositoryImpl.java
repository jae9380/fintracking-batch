package com.ft.batch.infrastructure.persistence;

import com.ft.batch.application.port.BatchUserRepository;
import com.ft.batch.domain.BatchUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BatchUserRepositoryImpl implements BatchUserRepository {

    private final JpaBatchUserRepository jpaBatchUserRepository;

    @Override
    public BatchUser save(BatchUser batchUser) {
        return jpaBatchUserRepository.save(batchUser);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaBatchUserRepository.existsByUserId(userId);
    }

    @Override
    public List<Long> findAllUserIds() {
        return jpaBatchUserRepository.findAllUserIds();
    }
}
