package com.notificationbackend.service;

import com.notification.common.dto.FcmTokenRequest;
import com.notification.common.dto.MPResponse;
import com.notification.common.dto.NotificationSendRequest;

public interface NotificationService {

    MPResponse updateUserFcmToken(FcmTokenRequest gcmTokenRequest) throws Exception;

    MPResponse sendNotification(NotificationSendRequest notificationSendRequest) throws Exception;
}
