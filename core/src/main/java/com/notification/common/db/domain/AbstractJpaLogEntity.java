package com.notification.common.db.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import com.notification.common.constants.CommonColumnNames;

@MappedSuperclass
@EntityListeners({ AbstractJpaLogEntity.AbstractEntityListener.class })
@SuppressWarnings("serial")
public class AbstractJpaLogEntity extends AbstractJpaEntity
{
	
	@Column(name = CommonColumnNames.ArchivedId)
	private long archivedId;
	
	public long getArchivedId()
	{
		return archivedId;
	}
	
	public void setArchivedId(long archivedId)
	{
		this.archivedId = archivedId;
	}
	
}
