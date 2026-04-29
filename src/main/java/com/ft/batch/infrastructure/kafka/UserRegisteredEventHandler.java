package com.ft.batch.infrastructure.kafka;

import com.ft.batch.application.port.BatchUserRepository;
import com.ft.batch.domain.BatchUser;
import com.ft.common.event.UserRegisteredEvent;
import com.ft.common.kafka.EventHandler;
import com.ft.common.kafka.KafkaTopic;
import com.ft.common.metric.annotation.MonitoredKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventHandler implements EventHandler<UserRegisteredEvent> {

    private final BatchUserRepository batchUserRepository;

    @MonitoredKafka(topic = KafkaTopic.USER_REGISTERED, action = "consume")
    @KafkaListener(topics = KafkaTopic.USER_REGISTERED, groupId = "batch-service")
    @Override
    public void handle(UserRegisteredEvent event) {
        if (batchUserRepository.existsByUserId(event.userId())) {
            log.debug("[Batch] 이미 등록된 사용자 스킵 — userId={}", event.userId());
            return;
        }

        batchUserRepository.save(BatchUser.create(event.userId(), event.email()));
        log.info("[Batch] BatchUser 등록 완료 — userId={}, email={}", event.userId(), event.email());
    }
}
