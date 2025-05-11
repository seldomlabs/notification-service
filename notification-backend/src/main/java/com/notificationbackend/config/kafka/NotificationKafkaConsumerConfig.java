package com.notificationbackend.config.kafka;

import com.notification.util.ApplicationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@EnableKafka
@Configuration
@PropertySource("file:/Users/harsh.shukla/notification-service-properties/properties/kafka.properties")
public class NotificationKafkaConsumerConfig extends AbstractKafkaConsumerConfig {
    private static final String KAFKA_NOTIFICATION_GROUP_ID = ApplicationProperties.getInstance().getProperty("kafka",
            "kafka.notification.group", "notification_group");

    private static final String KAFKA_NOTIFICATION_CONCURRENCY = ApplicationProperties.getInstance().getProperty("kafka",
            "kafka.notification.concurrency", "0");

    @Override
    public String groupId() {
        return KAFKA_NOTIFICATION_GROUP_ID;
    }

    @Override
    public String getKafkaConcurrency() {
        return KAFKA_NOTIFICATION_CONCURRENCY;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Integer, String> notificationKafkaListenerContainerFactory() {
        return createContainerFactory();
    }
}
