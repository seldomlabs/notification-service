package com.notificationbackend.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.notification.common.dto.NotificationEventDto;
import com.notificationbackend.service.NotificationSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pushNotificationServiceImpl")
public class PushNotificationServiceImpl implements NotificationSender {

    Logger logger = LogManager.getLogger(PushNotificationServiceImpl.class);

    @Autowired
    FirebaseMessaging firebaseMessaging;

    public void sendNotification(NotificationEventDto notificationEventDto) {
        try {
            String registrationToken = notificationEventDto.getGcmToken();
            String title = notificationEventDto.getTitle();
            String body = notificationEventDto.getBody();
            Message message = Message.builder().setNotification(Notification.builder().setTitle(title)
                    .setBody(body).build()).setToken(registrationToken).build();
            firebaseMessaging.send(message);
        } catch (Exception e) {
            logger.error("Exception in sendNotification", e);
        }
    }
}
