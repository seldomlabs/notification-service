package com.notification.common.kafka;

import com.notification.util.ApplicationProperties;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sahilmohindroo on 25/4/19
 */

@Configuration
@EnableKafka
@EnableTransactionManagement(proxyTargetClass = true)
public class KafkaTxProducerConfig {

    public static final String KAFKA_BOOTSTRAP_SERVERS = ApplicationProperties.getInstance().getProperty("kafka", "kafka.bootstrap.servers", "m-data-kafka004:9092,m-data-kafka005:9092,m-data-kafka006:9092");
    public static final String KAFKA_CLIENT_ID = ApplicationProperties.getInstance().getProperty("kafka", "KAFKA_CLIENT_ID", "aryan");

    @Bean
    public Map txProducerConfigurations() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.RETRIES_CONFIG, 2);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        // to ensure each message is flushed as soon as it is sent
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KAFKA_CLIENT_ID);
        //props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return props;
    }

    /*@Bean
    public ProducerFactory txProducerFactory() {
        Map configurations = txProducerConfigurations();
        //configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer.getCanonicalName());
        //configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer.getCanonicalName());

        DefaultKafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory(configurations);
        producerFactory.setTransactionIdPrefix("wallet.tx.id"+ RandomStringGenerator.generateAllAlphaNumericOtp(9));

        return producerFactory;
    }

    @Bean
    public KafkaTransactionManager kafkaTransactionManager(@Qualifier(value = "txProducerFactory") ProducerFactory producerFactory) {
        KafkaTransactionManager manager = new KafkaTransactionManager(producerFactory);
        manager.setFailEarlyOnGlobalRollbackOnly(true);
        manager.setNestedTransactionAllowed(true);
        manager.setValidateExistingTransaction(true);
        manager.setRollbackOnCommitFailure(true);
        manager.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
        return manager;
    }

    @Bean
    public KafkaTemplate kafkaTxTemplate(@Qualifier(value = "txProducerFactory") ProducerFactory txProducerFactory) {
        return new KafkaTemplate(txProducerFactory);
    }*/
}
