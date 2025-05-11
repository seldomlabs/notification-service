package com.notificationbackend.model;

import com.notification.common.db.domain.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_gcm_token_mapping")
public class UserGcmTokenMapping extends AbstractJpaEntity {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "gcm_token")
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
