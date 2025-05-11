package com.notificationbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"com.notificationbackend", "com.notification"})
public class NotificationBackendApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(NotificationBackendApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}