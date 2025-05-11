package com.notification.common.kafka;

import com.notification.util.MetricsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RetryErrorHandler extends DefaultErrorHandler {

    Logger logger = LogManager.getLogger(RetryErrorHandler.class);

    private static String DLQ_SUFFIX = "_dlq";
    private static final int MAX_RETRY_ATTEMPT = 3;
    private static String tagFormat = "topic:%s";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public RetryErrorHandler() {
    }

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * On error in Kafka Consumer, it will increase the retry count and send it again to the topic for retry.
     * If max retry attempts have been already made, it will made to the DLQ.
     * Any error in retrying will lead to the message being sent to DLQ.
     *
     * IMPORTANT: This will only work when the kafka message produced is encapsulated inside a
     * {@link RetryableKafkaRecord}. Otherwise, it will lead to an error and the message will be directly sent to
     * topic's DLQ.
     *
     *
     * @param thrownException exception that was thrown in kafka consumer
     * @param record Kafka record consumed
     * */
    public void handle(Exception thrownException, ConsumerRecord<?, ?> record) {
        logger.error("Error while processing: " + ObjectUtils.nullSafeToString(record), thrownException);
        String topic = record.topic();
        try {
            RetryableKafkaRecord retryableKafkaRecord = mapper.readValue((String) record.value(),
                    RetryableKafkaRecord.class);
            logger.info("present retry count is "+retryableKafkaRecord.getRetryCount() + " for " +
                    "message "+retryableKafkaRecord.getMessage());
            retryableKafkaRecord.setRetryCount(retryableKafkaRecord.getRetryCount() + 1);
            if(retryableKafkaRecord.getRetryCount() < MAX_RETRY_ATTEMPT) {
                MetricsUtil.publishCountMetric("kafka.consumer.error.retry.push",String.format(tagFormat,topic));
                kafkaTemplate.send(topic, mapper.writeValueAsString(retryableKafkaRecord)).get(10,
                        TimeUnit.SECONDS);
            } else{
                logger.info("Number of retry exceeded. Moving to DLQ topic");
                handleExceptionMoveDLQ(record);
            }
        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Exception in retry error handler", e);
            MetricsUtil.publishCountMetric("kafka.consumer.error.retry.push.failure",String.format(tagFormat,topic));
            handleExceptionMoveDLQ(record);
        }
    }

    /**
     * function to move record to DLQ.
     *
     * @param record : consumed Kafka record
     * */
    private void handleExceptionMoveDLQ(ConsumerRecord<?,?> record){
        String dlqTopic = record.topic().concat(DLQ_SUFFIX);
        try{
            MetricsUtil.publishCountMetric("kafka.consumer.error.dlq.push",String.format(tagFormat,dlqTopic));
            kafkaTemplate.send(dlqTopic, (String)record.value()).get(10, TimeUnit.SECONDS);
            MetricsUtil.publishCountMetric("kafka.consumer.error.dlq.push.SUCCESS",String.format(tagFormat,dlqTopic));
            logger.info("moved to dlq successfully");
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            logger.error("exception while sending to dlq", e);
            MetricsUtil.publishCountMetric("kafka.consumer.error.dlq.push.FAILURE",String.format(tagFormat,dlqTopic));
        }
    }

}
