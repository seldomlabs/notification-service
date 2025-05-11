package com.notification.common.db.domain;

import jakarta.persistence.*;

@MappedSuperclass
public  class AbstractJpaArchiveEntity extends AbstractJpaEntity {

    @Transient
    public Boolean archived=false;

    public Boolean getArchived() {
        return archived;
    }
}
