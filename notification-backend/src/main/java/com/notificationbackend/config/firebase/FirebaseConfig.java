package com.notificationbackend.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.notification.constants.NotificationConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials firebaseGoogleCredentials) {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(firebaseGoogleCredentials)
                .build();

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    GoogleCredentials firebaseGoogleCredentials() throws IOException {

        try (FileInputStream serviceAccountStream = new FileInputStream(NotificationConstants.FIREBASE_SERVICE_ACCOUNT_KEY)) {
            return GoogleCredentials.fromStream(serviceAccountStream);
        } catch (IOException e) {
            // Handle the exception, such as logging or rethrowing it
            throw new IOException("Failed to load credentials from the service account file.", e);
        }
    }
}
