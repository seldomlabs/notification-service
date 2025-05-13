package com.notification.common.dto;

import com.notification.constants.GlobalConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class FcmTokenRequest {

    @NotNull(message = GlobalConstants.ValidationMessages.DATA_INVALID)
    @NotEmpty(message = "User ID cannot be empty")
    private String userId;

    @NotNull(message = GlobalConstants.ValidationMessages.DATA_INVALID)
    @NotEmpty(message = "Fcm token cannot be empty")
    private String fcmToken;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
