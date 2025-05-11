package com.notification.common.dto;

import com.notification.constants.GlobalConstants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class GcmTokenRequest {

    @NotNull(message = GlobalConstants.ValidationMessages.DATA_INVALID)
    @NotEmpty(message = "User ID cannot be empty")
    private String userId;

    @NotNull(message = GlobalConstants.ValidationMessages.DATA_INVALID)
    @NotEmpty(message = "Gcm token cannot be empty")
    private String gcmToken;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }
}
