package com.notificationbackend.config.kafka;

import com.notification.util.ApplicationProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;


public class AbstractKafkaConsumerConfig {

    protected static final String KAFKA_BOOTSTRAP_SERVERS = ApplicationProperties.getInstance().getProperty("kafka", "kafka.bootstrap.servers",
            "localhost:9092");

    public String groupId() {
        return "";
    }

    public String getKafkaConcurrency() {
        return "";
    }

    private final static Logger logger = LogManager.getLogger(AbstractKafkaConsumerConfig.class);

    public ConcurrentKafkaListenerContainerFactory<Integer, String> createContainerFactory() {
        logger.info("+++Abstractconfig: calling createContainerFactory from abstract");
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        logger.info("+++Abstractconfig: " + factory.getConsumerFactory().getConfigurationProperties());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        if (Integer.parseInt(getKafkaConcurrency()) > 0) {
            factory.setConcurrency(Integer.parseInt(getKafkaConcurrency()));
        }
        return factory;
    }

    private ConsumerFactory<Integer, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    private Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        logger.error("KAFKA_BOOTSTRAP_SERVERS " + KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 300000);
        return props;
    }
}