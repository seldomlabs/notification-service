package com.notification.common.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationEventDto {

    private String notificationType;

    private String title;

    private String body;

    private String gcmToken;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public NotificationEventDto(NotificationEventDtoBuilder notificationEventDtoBuilder) {
        this.notificationType = notificationEventDtoBuilder.notificationType;
        this.title = notificationEventDtoBuilder.title;
        this.body = notificationEventDtoBuilder.body;
        this.gcmToken = notificationEventDtoBuilder.gcmToken;
    }

    public static class NotificationEventDtoBuilder {
        private String notificationType;
        private String title;
        private String body;
        private String gcmToken;

        public NotificationEventDtoBuilder notificationType(String notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        public NotificationEventDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationEventDtoBuilder body(String body) {
            this.body = body;
            return this;
        }

        public NotificationEventDtoBuilder gcmToken(String gcmToken) {
            this.gcmToken = gcmToken;
            return this;
        }

        public NotificationEventDto build(){
            return new NotificationEventDto(this);
        }
    }

}
