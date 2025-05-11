package com.notificationbackend.service;

import com.notification.common.dto.GcmTokenRequest;
import com.notification.common.dto.MPResponse;
import com.notification.common.dto.NotificationSendRequest;

public interface NotificationService {

    MPResponse updateUserGcmToken(GcmTokenRequest gcmTokenRequest) throws Exception;

    MPResponse sendNotification(NotificationSendRequest notificationSendRequest) throws Exception;
}
