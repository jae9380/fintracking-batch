package com.ft.batch.application;

import com.ft.common.event.BatchReportEvent;
import com.ft.common.kafka.AbstractEventPublisher;
import com.ft.common.kafka.KafkaTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BatchReportEventPublisher extends AbstractEventPublisher<BatchReportEvent> {

    public BatchReportEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    protected String topic() {
        return KafkaTopic.BATCH_REPORT;
    }
}
