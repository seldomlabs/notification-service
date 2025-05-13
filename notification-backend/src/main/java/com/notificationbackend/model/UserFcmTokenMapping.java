package com.notificationbackend.model;

import com.notification.common.db.domain.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_fcm_token_mapping")
public class UserFcmTokenMapping extends AbstractJpaEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "fcm_token")
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
