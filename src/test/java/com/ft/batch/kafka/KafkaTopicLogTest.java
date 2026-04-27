package com.ft.batch.kafka;

import com.ft.batch.application.BatchReportEventPublisher;
import com.ft.common.event.BatchReportEvent;
import com.ft.common.kafka.KafkaTopic;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Kafka 토픽 발행 로그 테스트 — batch-service
 *
 * 실제 Kafka 브로커 없이 KafkaTemplate을 Mock 처리하고,
 * ArgumentCaptor로 실제 발행될 토픽명과 페이로드를 캡처하여 콘솔에 출력한다.
 *
 * 테스트 실행 시 콘솔에서 아래 형태의 로그를 확인할 수 있다:
 *   [KAFKA-LOG] topic=batch.report | payload=BatchReportEvent{...}
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class KafkaTopicLogTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    BatchReportEventPublisher publisher;

    @BeforeEach
    void setUp() {
        given(kafkaTemplate.send(anyString(), any())).willReturn(null);
        publisher = new BatchReportEventPublisher(kafkaTemplate);
    }

    @Test
    void publish_batchReport_logsTopicAndPayload() {
        BatchReportEvent event = new BatchReportEvent(
                "evt-001", 1L, "2026-04",
                "[2026-04] 월간 리포트",
                "총 수입: 2,500,000원 | 총 지출: 1,200,000원 | 순수익: 1,300,000원"
        );

        publisher.publish(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), payloadCaptor.capture());

        BatchReportEvent captured = (BatchReportEvent) payloadCaptor.getValue();

        log.info("┌─────────────────────────────────────────────────");
        log.info("│ [KAFKA-LOG] batch-service → PUBLISH");
        log.info("│ topic     : {}", topicCaptor.getValue());
        log.info("│ eventId   : {}", captured.eventId());
        log.info("│ userId    : {}", captured.userId());
        log.info("│ yearMonth : {}", captured.yearMonth());
        log.info("│ title     : {}", captured.title());
        log.info("│ message   : {}", captured.message());
        log.info("└─────────────────────────────────────────────────");

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaTopic.BATCH_REPORT);
        assertThat(captured.yearMonth()).isEqualTo("2026-04");
    }
}
