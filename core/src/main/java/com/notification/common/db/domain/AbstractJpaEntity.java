package com.notification.common.db.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlTransient;

import com.notification.common.constants.CommonColumnNames;
import com.notification.common.dto.CommonDto;

/**
 * Abstract JPA super class with generic ID.
 * 
 * @author abhishek
 * 		
 */
@MappedSuperclass
@EntityListeners({ AbstractJpaEntity.AbstractEntityListener.class })
@SuppressWarnings("serial")
public abstract class AbstractJpaEntity implements CommonDto, Serializable
{
	
	public static class AbstractEntityListener
	{
		
		// This code is executed on every insert.
		@PrePersist
		public void onPrePersist(AbstractJpaEntity abstractEntity)
		{
			abstractEntity.initEntity();
		}
		
		// This code is executed on every update.
		@PreUpdate
		public void onPreUpdate(AbstractJpaEntity abstractEntity)
		{
			abstractEntity.preUpdate();
		}
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	//@JsonIgnore
	@XmlTransient
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	@Column(name = CommonColumnNames.CreateDate)//, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;
	
	@Column(name = CommonColumnNames.UpdateDate)//, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;
	
	@Column(name = CommonColumnNames.Shadowed)//, columnDefinition = "BOOLEAN DEFAULT 0")
	private boolean shadowed;
	
	//@JsonIgnore
	@XmlTransient
	public Date getUpdateDate()
	{
		return updateDate;
	}
	
	public void setUpdateDate(Date updateDate)
	{
		this.updateDate = updateDate;
	}
	
	/**
	 * @return the createDate
	 */
	//@JsonIgnore
	@XmlTransient
	public Date getCreateDate()
	{
		return createDate;
	}
	
	public void setCreateDate(Date createDate)
	{
		this.createDate = createDate;
	}
	
	public boolean isShadowed()
	{
		return shadowed;
	}
	
	public void setShadowed(boolean shadowed)
	{
		this.shadowed = shadowed;
	}
	
	void initEntity()
	{
		createDate = updateDate = Optional.ofNullable(this.createDate).orElse(new Date());
	}
	
	void preUpdate()
	{
		updateDate = new Date();
	}
	

	public boolean isNewInstance()
	{
		return this.getCreateDate() == null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof AbstractJpaEntity)
		{
			if (id != 0)
			{
				return id == (((AbstractJpaEntity) obj).id);
			}
		}
		return false;
	}
}
