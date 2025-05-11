package com.notificationapi.constant;

import com.notification.util.ApplicationProperties;

public class ExternalHosts {

    public static final String USER_SERVICE_HOST = ApplicationProperties.getInstance()
            .getProperty("external", "user.service.host", "http://3.185.76:3000/");

    public static final String SUBSCRIPTION_SERVICE_HOST = ApplicationProperties.getInstance()
            .getProperty("external", "subscription.service.host", "http://3.185.76:3000/");
}
