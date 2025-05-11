package com.notificationbackend.service.kafka;

import com.notification.common.dto.NotificationEventDto;
import com.notification.constants.GlobalConstants;
import com.notificationbackend.service.impl.PushNotificationServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service("notificationKafkaConsumer")
public class NotificationKafkaConsumer {

    Logger logger = LogManager.getLogger(NotificationKafkaConsumer.class);

    @Autowired
    PushNotificationServiceImpl pushNotificationService;

    @KafkaListener(topics = "user_notification", autoStartup = "${kafka.user.notification.consumer.auto.startup}", containerFactory = "notificationKafkaListenerContainerFactory")
    public void sendNotification(String message, Acknowledgment acknowledgment) {
        try {
            logger.info(message);
            NotificationEventDto notificationEvent = GlobalConstants.objectMapper.readValue(message, NotificationEventDto.class);
            pushNotificationService.sendNotification(notificationEvent);
        } catch (Exception e) {
            logger.error("Exception in sendNotification", e);
        }
        acknowledgment.acknowledge();
    }
}
