
package com.notification.common.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.notification.util.ApplicationProperties;

@Configuration
@EnableKafka
public class KafkaProducerConfig
{

	public static final String KAFKA_BOOTSTRAP_SERVERS = ApplicationProperties.getInstance().getProperty("kafka", "kafka.bootstrap.servers", "localhost:9092");

	@Bean
	public ProducerFactory<String, String> producerFactory()
	{
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
		props.put(ProducerConfig.RETRIES_CONFIG, 2);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 200000);
		// to ensure each message is flushed as soon as it is sent
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
		props.put(ProducerConfig.ACKS_CONFIG, "1");

		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate()
	{
		return new KafkaTemplate<>(producerFactory());
	}

}
