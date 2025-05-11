package com.notification.common.db;

import java.sql.SQLException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

public class CustomJpaDialect extends HibernateJpaDialect
{
	
	private static final long serialVersionUID = 5821616141322634602L;
	
	Logger logger = LogManager.getLogger(CustomJpaDialect.class);
	
	// @Override
	// public ConnectionHandle getJdbcConnection(EntityManager entityManager, boolean readOnly) throws PersistenceException, SQLException
	// {
	// ConnectionHandle connectionHandle = super.getJdbcConnection(entityManager, readOnly);
	// if (null != connectionHandle && null != connectionHandle.getConnection())
	// {
	// if (readOnly)
	// {
	// connectionHandle.getConnection().setReadOnly(true);
	// }
	// else
	// {
	// connectionHandle.getConnection().setReadOnly(false);
	// }
	// }
	// return connectionHandle;
	// }
	
	@Override
	public Object beginTransaction(EntityManager entityManager, TransactionDefinition definition) throws PersistenceException, SQLException, TransactionException
	{
		if (definition.isReadOnly())
		{
			logger.debug("using read-only datasource");
			DatasourceProvider.setDatasource(AvailableDataSources.READ);
		}
		else 
		{
			logger.debug("using read-write datasource");
			DatasourceProvider.setDatasource(AvailableDataSources.WRITE);
		}
		return super.beginTransaction(entityManager, definition);
	}
}
