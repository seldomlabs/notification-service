package com.notificationbackend.service.impl;

import com.notification.common.db.service.CommonDbService;
import com.notification.common.dto.*;
import com.notification.common.kafka.KafkaPushService;
import com.notification.constants.GlobalConstants;
import com.notificationbackend.dao.NotificationDao;
import com.notificationbackend.model.UserFcmTokenMapping;
import com.notificationbackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.notification.constants.NotificationConstants.NotificationTopics;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    NotificationDao notificationDao;

    @Autowired
    CommonDbService commonDbService;

    //@Resource(name = "getNotificationSendStrategies")
    //Map<String, NotificationSender> notificationSendStrategyMap;

    @Autowired
    private KafkaPushService kafkaPushService;

    @Override
    public MPResponse updateUserFcmToken(FcmTokenRequest request) throws Exception {
        MPResponse mpResponse = new MPResponse();
        mpResponse.setStatus(MPResponseStatus.FAILURE.name());

        String userId = request.getUserId();
        UserFcmTokenMapping userFcmTokenMapping = notificationDao.getUserFcmTokenMappingFromUserId(userId);

        if (userFcmTokenMapping == null) {
            userFcmTokenMapping = new UserFcmTokenMapping();
            userFcmTokenMapping.setUserId(userId);
        }
        userFcmTokenMapping.setFcmToken(request.getFcmToken());
        commonDbService.updateEntity(userFcmTokenMapping);
        mpResponse.setStatus(MPResponseStatus.SUCCESS.name());
        mpResponse.setMessage("Gcm token updated successfully");
        return mpResponse;
    }

    @Override
    public MPResponse sendNotification(NotificationSendRequest request) throws Exception {
        MPResponse mpResponse = new MPResponse();
        mpResponse.setStatus(MPResponseStatus.FAILURE.name());

        String userId = request.getUserId();
        String notificationType = request.getNotificationType();
        String body = request.getBody();
        String title = request.getTitle();
        UserFcmTokenMapping userGcmTokenMapping = notificationDao.getUserFcmTokenMappingFromUserId(userId);
        if (userGcmTokenMapping == null) {
            return mpResponse;
        }
        NotificationEventDto notificationEventDto = new NotificationEventDto.NotificationEventDtoBuilder().notificationType(notificationType)
                .body(body).title(title).gcmToken(userGcmTokenMapping.getFcmToken()).build();
        kafkaPushService.sendToKafka(NotificationTopics.user_notification.name(), GlobalConstants.objectMapper.writeValueAsString(notificationEventDto));
        mpResponse.setStatus(MPResponseStatus.SUCCESS.name());
        mpResponse.setMessage("Successfully pushed notification");
        return mpResponse;
    }
}
