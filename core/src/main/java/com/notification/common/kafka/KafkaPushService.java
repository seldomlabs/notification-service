package com.notification.common.kafka;

import com.notification.util.MetricsUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaPushService {

    private @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    private Logger logger = LogManager.getLogger(KafkaPushService.class);

    private static String TOPIC_TAG = "topic:%s";

    public void sendToKafka(String topicName, String data) {
        try {
            MetricsUtil.publishCountMetricWithReason("Kafka Aryan Total Calls", topicName, String.format(TOPIC_TAG, topicName));
            kafkaTemplate.send(topicName, data).get();
            MetricsUtil.publishCountMetricWithReason("Kafka Aryan Success", topicName, String.format(TOPIC_TAG, topicName));
        } catch (Exception e) {
            logger.error("Exception in sendToKafka", e);
            MetricsUtil.publishCountMetricWithReason("Kafka Aryan Failure", topicName, String.format(TOPIC_TAG, topicName));
        }
    }
}
