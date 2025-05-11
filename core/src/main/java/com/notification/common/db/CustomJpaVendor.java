package com.notification.common.db;

import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

public class CustomJpaVendor extends HibernateJpaVendorAdapter
{
	
	private final HibernateJpaDialect jpaDialect = new CustomJpaDialect();
	
	@Override
	public HibernateJpaDialect getJpaDialect()
	{
		return this.jpaDialect;
	}
}
