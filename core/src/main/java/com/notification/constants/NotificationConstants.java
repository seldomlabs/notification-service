package com.notification.constants;

import com.notification.util.ApplicationProperties;

public class NotificationConstants {

    public static String FIREBASE_SERVICE_ACCOUNT_KEY = ApplicationProperties.getInstance().getProperty("google",
            "firebase.service.key.path");

    public enum NotificationTopics {
        user_notification
    }
}
